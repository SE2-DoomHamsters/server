package com.doomhamsters.lobby;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Authoritative in-memory service for lobby membership and game-start state.
 */
@Service
public class LobbyService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LobbyService.class);
  private static final int LOBBY_ID_LENGTH = 8;

  private final Map<String, Lobby> activeLobbies = new ConcurrentHashMap<>();
  private final int defaultMaxPlayers;
  private final Duration memberTimeout;
  private final Clock clock;
  private final LobbyRealtimePublisher realtimePublisher;

  /** Constructor used by focused unit tests. */
  public LobbyService() {
    this(6, Duration.ofMinutes(2), Clock.systemUTC(), null);
  }

  /**
   * Spring constructor with configurable capacity and stale-member TTL.
   *
   * @param defaultMaxPlayers configured maximum lobby capacity
   * @param memberTimeoutMillis stale-member timeout in milliseconds
   * @param publisherProvider optional realtime publisher
   */
  @Autowired
  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  public LobbyService(
      @Value("${doomhamsters.lobby.max-players:6}") int defaultMaxPlayers,
      @Value("${doomhamsters.lobby.member-timeout-ms:120000}") long memberTimeoutMillis,
      ObjectProvider<LobbyRealtimePublisher> publisherProvider) {
    this(
        defaultMaxPlayers,
        Duration.ofMillis(memberTimeoutMillis),
        Clock.systemUTC(),
        publisherProvider.getIfAvailable());
  }

  LobbyService(
      int defaultMaxPlayers,
      Duration memberTimeout,
      Clock clock,
      LobbyRealtimePublisher realtimePublisher) {
    this.defaultMaxPlayers = Math.max(6, defaultMaxPlayers);
    this.memberTimeout = memberTimeout;
    this.clock = clock;
    this.realtimePublisher = realtimePublisher;
  }

  /**
   * Erstellt eine neue Lobby basierend auf einem Gruppennamen.
   *
   * @param groupName Der eingegebene Name der Gruppe
   * @param creator Der Benutzer, der diese Lobby erstellt
   * @return Die neu generierte Lobby
   */
  public Lobby createLobby(String groupName, User creator) {
    validateUser(creator);

    String lobbyId = newLobbyId();
    Lobby lobby = new Lobby(lobbyId);
    User host = connectedCopy(creator, Instant.now(clock));

    lobby.setGroupName(groupName);
    lobby.setHostId(host.getId());
    lobby.setMaxPlayers(defaultMaxPlayers);
    lobby.setMembers(List.of(host));
    lobby.setQrCodeBase64(generateQrCode(lobbyId));
    lobby.incrementVersion();

    activeLobbies.put(lobbyId, lobby);
    LOGGER.info(
        "lobby create accepted: lobbyId={}, creatorId={}",
        lobbyId,
        host.getId());

    return new Lobby(lobby);
  }

  /**
   * Fügt einen User einer bestehenden Lobby hinzu oder aktualisiert seine Daten.
   *
   * @param lobbyId Die ID der Lobby, der beigetreten werden soll
   * @param user Der Benutzer, der beitritt oder aktualisiert wird
   * @return Ein Optional mit der aktualisierten Lobby, falls sie existiert
   */
  public Optional<Lobby> joinOrUpdateLobby(String lobbyId, User user) {
    validateUser(user);

    Lobby lobby = getCanonicalLobby(lobbyId);
    if (lobby == null) {
      LOGGER.warn("join rejected: lobbyId={}, userId={}, reason=not_found", lobbyId, user.getId());
      return Optional.empty();
    }

    synchronized (lobby) {
      removeExpiredMembersLocked(lobby, Instant.now(clock));

      if (lobby.isGameStarted()) {
        LOGGER.warn(
            "join rejected: lobbyId={}, userId={}, reason=already_started",
            lobby.getLobbyId(),
            user.getId());
        throw new IllegalStateException("Game already started");
      }

      List<User> members = new ArrayList<>(lobby.getMembers());
      Optional<User> existing =
          members.stream().filter(member -> member.getId().equals(user.getId())).findFirst();

      if (existing.isPresent()) {
        User member = existing.get();
        member.setUsername(user.getUsername());
        member.setAvatar(user.getAvatar());
        member.markSeen(Instant.now(clock));
        lobby.setMembers(members);
        lobby.incrementVersion();
        LOGGER.info(
            "reconnect accepted: lobbyId={}, userId={}, memberCount={}",
            lobby.getLobbyId(),
            user.getId(),
            members.size());
        return Optional.of(new Lobby(lobby));
      }

      members.add(connectedCopy(user, Instant.now(clock)));
      lobby.setMembers(members);
      lobby.incrementVersion();

      LOGGER.info(
          "join accepted: lobbyId={}, userId={}, memberCount={}",
          lobby.getLobbyId(),
          user.getId(),
          members.size());

      return Optional.of(new Lobby(lobby));
    }
  }

  /**
   * Updates a member heartbeat and returns the current authoritative snapshot.
   *
   * @param lobbyId lobby to update
   * @param userId member reporting the heartbeat
   * @return updated lobby snapshot, or empty if the lobby or member does not exist
   */
  public Optional<Lobby> heartbeat(String lobbyId, String userId) {
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("userId is required");
    }

    Lobby lobby = getCanonicalLobby(lobbyId);
    if (lobby == null) {
      return Optional.empty();
    }

    synchronized (lobby) {
      removeExpiredMembersLocked(lobby, Instant.now(clock));
      List<User> members = new ArrayList<>(lobby.getMembers());
      Optional<User> member =
          members.stream().filter(candidate -> candidate.getId().equals(userId)).findFirst();
      if (member.isEmpty()) {
        return Optional.empty();
      }
      member.get().markSeen(Instant.now(clock));
      lobby.setMembers(members);
      lobby.incrementVersion();
      LOGGER.info("heartbeat accepted: lobbyId={}, userId={}", lobby.getLobbyId(), userId);
      return Optional.of(new Lobby(lobby));
    }
  }

  /**
   * Removes a user from the lobby and reassigns host to the oldest remaining member.
   *
   * @param lobbyId lobby to leave
   * @param userId member leaving the lobby
   * @return updated lobby snapshot, or empty if the lobby does not exist
   */
  public Optional<Lobby> leaveLobby(String lobbyId, String userId) {
    if (lobbyId == null || userId == null) {
      return Optional.empty();
    }

    Lobby lobby = getCanonicalLobby(lobbyId);
    if (lobby == null) {
      return Optional.empty();
    }

    synchronized (lobby) {
      List<User> members = new ArrayList<>(lobby.getMembers());
      boolean removed = members.removeIf(member -> member.getId().equals(userId));

      if (!removed) {
        return Optional.of(new Lobby(lobby));
      }

      LOGGER.info("leave accepted: lobbyId={}, userId={}", lobby.getLobbyId(), userId);

      if (members.isEmpty()) {
        activeLobbies.remove(lobby.getLobbyId(), lobby);
        LOGGER.info("lobby removed after last member left: lobbyId={}", lobby.getLobbyId());
        return Optional.empty();
      }

      reassignHostIfNeededLocked(lobby, userId, members);
      lobby.setMembers(members);
      lobby.incrementVersion();
      return Optional.of(new Lobby(lobby));
    }
  }

  /**
   * Starts a lobby game atomically and idempotently.
   *
   * @param lobbyId lobby identifier
   * @param userId lobby member identifier
   * @param gameCreator callback that creates exactly one game for a valid first start
   * @return start result, if the lobby exists
   */
  public Optional<GameStartOutcome> startGame(
      String lobbyId,
      String userId,
      Function<Lobby, String> gameCreator) {
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("A lobby member id is required");
    }

    Lobby lobby = getCanonicalLobby(lobbyId);
    if (lobby == null) {
      return Optional.empty();
    }

    synchronized (lobby) {
      removeExpiredMembersLocked(lobby, Instant.now(clock));
      List<User> activeMembers = activeMembers(lobby);

      boolean initiatedByMember = activeMembers.stream()
          .anyMatch(member -> userId.equals(member.getId()));
      if (!initiatedByMember) {
        LOGGER.warn(
            "start rejected: lobbyId={}, userId={}, reason=not_member",
            lobby.getLobbyId(),
            userId);
        throw new SecurityException("Only lobby members can start the game");
      }

      if (lobby.isGameStarted() && lobby.getGameId() != null) {
        LOGGER.info(
            "start accepted idempotent: lobbyId={}, userId={}, gameId={}",
            lobby.getLobbyId(),
            userId,
            lobby.getGameId());
        return Optional.of(new GameStartOutcome(new Lobby(lobby), lobby.getGameId(), false));
      }

      if (activeMembers.size() < 2) {
        LOGGER.warn(
            "start rejected: lobbyId={}, userId={}, reason=too_few_active_players, activeCount={}",
            lobby.getLobbyId(),
            userId,
            activeMembers.size());
        throw new IllegalStateException("At least 2 active players are required");
      }

      Lobby startSnapshot = new Lobby(lobby);
      startSnapshot.setMembers(activeMembers);
      String gameId = gameCreator.apply(startSnapshot);

      lobby.setMembers(activeMembers);
      lobby.setGameId(gameId);
      lobby.setGameStarted(true);
      lobby.incrementVersion();

      LOGGER.info(
          "start accepted: lobbyId={}, userId={}, gameId={}, activeCount={}",
          lobby.getLobbyId(),
          userId,
          gameId,
          activeMembers.size());

      return Optional.of(new GameStartOutcome(new Lobby(lobby), gameId, true));
    }
  }

  /**
   * Stores the started game id for a lobby.
   *
   * @param lobbyId lobby to mark as started
   * @param gameId started game id
   * @return updated lobby snapshot, or empty if the lobby does not exist
   */
  @SuppressWarnings("UnusedReturnValue")
  public Optional<Lobby> markGameStarted(String lobbyId, String gameId) {
    Lobby lobby = getCanonicalLobby(lobbyId);
    if (lobby == null) {
      return Optional.empty();
    }

    synchronized (lobby) {
      lobby.setGameId(gameId);
      lobby.setGameStarted(true);
      lobby.incrementVersion();
      return Optional.of(new Lobby(lobby));
    }
  }

  /**
   * Returns the authoritative snapshot for a lobby ID.
   *
   * @param lobbyId lobby to fetch
   * @return lobby snapshot, or {@code null} if no lobby exists
   */
  public Lobby getLobby(String lobbyId) {
    Lobby lobby = getCanonicalLobby(lobbyId);
    if (lobby == null) {
      return null;
    }

    synchronized (lobby) {
      removeExpiredMembersLocked(lobby, Instant.now(clock));
      return new Lobby(lobby);
    }
  }

  /**
   * Removes expired pre-game members and returns changed lobby snapshots.
   *
   * @return lobby snapshots changed by cleanup
   */
  public List<Lobby> cleanupExpiredMembers() {
    Instant now = Instant.now(clock);
    List<Lobby> changed = new ArrayList<>();

    activeLobbies.forEach((id, ignored) -> {
      Lobby activeLobby = activeLobbies.get(id);
      if (activeLobby == null) {
        return;
      }
      synchronized (activeLobby) {
        int before = activeLobby.getMembers().size();
        removeExpiredMembersLocked(activeLobby, now);
        int after = activeLobby.getMembers().size();
        if (before != after && activeLobbies.containsKey(id)) {
          changed.add(new Lobby(activeLobby));
        }
      }
    });

    return changed;
  }

  @Scheduled(fixedDelayString = "${doomhamsters.lobby.cleanup-interval-ms:30000}")
  void scheduledCleanupExpiredMembers() {
    if (realtimePublisher == null) {
      cleanupExpiredMembers();
      return;
    }

    for (Lobby snapshot : cleanupExpiredMembers()) {
      realtimePublisher.broadcastLobbySnapshot(snapshot);
    }
  }

  private Lobby getCanonicalLobby(String lobbyId) {
    if (lobbyId == null) {
      return null;
    }
    return activeLobbies.get(lobbyId.toUpperCase(Locale.ROOT));
  }

  private String newLobbyId() {
    String lobbyId;
    do {
      lobbyId = UUID.randomUUID()
          .toString()
          .replace("-", "")
          .substring(0, LOBBY_ID_LENGTH)
          .toUpperCase(Locale.ROOT);
    } while (activeLobbies.containsKey(lobbyId));
    return lobbyId;
  }

  private void removeExpiredMembersLocked(Lobby lobby, Instant now) {
    if (lobby.isGameStarted()) {
      return;
    }

    List<User> members = new ArrayList<>(lobby.getMembers());
    List<String> removedIds = members.stream()
        .filter(member -> isExpired(member, now))
        .map(User::getId)
        .toList();

    if (removedIds.isEmpty()) {
      return;
    }

    members.removeIf(member -> removedIds.contains(member.getId()));
    for (String removedId : removedIds) {
      LOGGER.info(
          "stale member removed: lobbyId={}, userId={}, timeoutMs={}",
          lobby.getLobbyId(),
          removedId,
          memberTimeout.toMillis());
    }

    if (members.isEmpty()) {
      activeLobbies.remove(lobby.getLobbyId(), lobby);
      LOGGER.info("lobby removed after stale cleanup: lobbyId={}", lobby.getLobbyId());
      return;
    }

    if (removedIds.contains(lobby.getHostId())) {
      lobby.setHostId(members.getFirst().getId());
      LOGGER.info(
          "host reassigned: lobbyId={}, newHostId={}, reason=host_expired",
          lobby.getLobbyId(),
          lobby.getHostId());
    }

    lobby.setMembers(members);
    lobby.incrementVersion();
  }

  private boolean isExpired(User member, Instant now) {
    Instant lastSeenAt = member.getLastSeenAt();
    return lastSeenAt != null && lastSeenAt.plus(memberTimeout).isBefore(now);
  }

  private void reassignHostIfNeededLocked(Lobby lobby, String removedUserId, List<User> members) {
    if (!removedUserId.equals(lobby.getHostId())) {
      return;
    }

    lobby.setHostId(members.getFirst().getId());
    LOGGER.info(
        "host reassigned: lobbyId={}, newHostId={}, reason=host_left",
        lobby.getLobbyId(),
        lobby.getHostId());
  }

  private List<User> activeMembers(Lobby lobby) {
    return lobby.getMembers().stream()
        .filter(User::isConnected)
        .toList();
  }

  private User connectedCopy(User user, Instant seenAt) {
    User copy = new User(user);
    copy.markSeen(seenAt);
    return copy;
  }

  private void validateUser(User user) {
    if (user == null || user.getId() == null || user.getId().isBlank()) {
      throw new IllegalArgumentException("User id is required");
    }
  }

  private String generateQrCode(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    try {
      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
      return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    } catch (com.google.zxing.WriterException | java.io.IOException e) {
      LOGGER.error("Failed to generate QR code for text: {}", text, e);
      return null;
    }
  }

  /**
   * Result of an idempotent lobby start attempt.
   *
   * @param lobby lobby snapshot
   * @param gameId shared game ID
   * @param created whether this call created the game
   */
  public record GameStartOutcome(Lobby lobby, String gameId, boolean created) {
    /**
     * Creates an immutable game start outcome.
     */
    public GameStartOutcome {
      lobby = new Lobby(lobby);
    }

    /**
     * Returns a defensive lobby snapshot.
     *
     * @return lobby snapshot copy
     */
    @Override
    public Lobby lobby() {
      return new Lobby(lobby);
    }
  }
}

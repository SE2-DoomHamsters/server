package com.doomhamsters.lobby;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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

  // Dependency injection for the infrastructure
  private final QrCodeGeneratorService qrCodeGenerator;

  /** Constructor used by focused unit tests. */
  public LobbyService(QrCodeGeneratorService qrCodeGenerator) {
    this(6, Duration.ofMinutes(2), Clock.systemUTC(), null, qrCodeGenerator);
  }

  /**
   * Spring constructor with configurable capacity and stale-member TTL.
   */
  @Autowired
  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  public LobbyService(
      @Value("${doomhamsters.lobby.max-players:6}") int defaultMaxPlayers,
      @Value("${doomhamsters.lobby.member-timeout-ms:120000}") long memberTimeoutMillis,
      ObjectProvider<LobbyRealtimePublisher> publisherProvider,
      QrCodeGeneratorService qrCodeGenerator) {
    this(
        defaultMaxPlayers,
        Duration.ofMillis(memberTimeoutMillis),
        Clock.systemUTC(),
        publisherProvider.getIfAvailable(),
        qrCodeGenerator);
  }

  LobbyService(
      int defaultMaxPlayers,
      Duration memberTimeout,
      Clock clock,
      LobbyRealtimePublisher realtimePublisher,
      QrCodeGeneratorService qrCodeGenerator) {
    this.defaultMaxPlayers = Math.max(6, defaultMaxPlayers);
    this.memberTimeout = memberTimeout;
    this.clock = clock;
    this.realtimePublisher = realtimePublisher;
    this.qrCodeGenerator = qrCodeGenerator;
  }

  /**
   * Creates a new lobby based on a group name.
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

    // Delegation to the new QRCode provider
    lobby.setQrCodeBase64(qrCodeGenerator.generateQrCode(lobbyId));
    lobby.incrementVersion();

    activeLobbies.put(lobbyId, lobby);
    LOGGER.info("lobby create accepted: lobbyId={}, creatorId={}", lobbyId, host.getId());

    return new Lobby(lobby);
  }

  /**
   * Joins an existing lobby or updates member details.
   */
  public Optional<Lobby> joinOrUpdateLobby(String lobbyId, User user) {
    validateUser(user);

    Lobby lobby = getCanonicalLobby(lobbyId);
    if (lobby == null) {
      LOGGER.warn("join rejected: lobbyId={}, userId={}, reason=not_found",
          lobbyId, user.getId());
      return Optional.empty();
    }

    synchronized (lobby) {
      removeExpiredMembersLocked(lobby, Instant.now(clock));

      if (lobby.isGameStarted()) {
        LOGGER.warn("join rejected: lobbyId={}, userId={}, reason=already_started",
            lobby.getLobbyId(), user.getId());
        throw new IllegalStateException("Game already started");
      }

      List<User> members = new ArrayList<>(lobby.getMembers());
      Optional<User> existing = members.stream()
          .filter(member -> member.getId().equals(user.getId()))
          .findFirst();

      if (existing.isPresent()) {
        User member = existing.get();
        member.setUsername(user.getUsername());
        member.setAvatar(user.getAvatar());
        member.markSeen(Instant.now(clock));
        lobby.setMembers(members);
        lobby.incrementVersion();
        LOGGER.info("reconnect accepted: lobbyId={}, userId={}, memberCount={}",
            lobby.getLobbyId(), user.getId(), members.size());
        return Optional.of(new Lobby(lobby));
      }

      members.add(connectedCopy(user, Instant.now(clock)));
      lobby.setMembers(members);
      lobby.incrementVersion();

      LOGGER.info("join accepted: lobbyId={}, userId={}, memberCount={}",
          lobby.getLobbyId(), user.getId(), members.size());
      return Optional.of(new Lobby(lobby));
    }
  }

  /**
   * Updates the last seen timestamp for a user.
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
      Optional<User> member = members.stream()
          .filter(candidate -> candidate.getId().equals(userId))
          .findFirst();
      if (member.isEmpty()) {
        return Optional.empty();
      }
      member.get().markSeen(Instant.now(clock));
      lobby.setMembers(members);
      lobby.incrementVersion();
      LOGGER.info("heartbeat accepted: lobbyId={}, userId={}",
          lobby.getLobbyId(), userId);
      return Optional.of(new Lobby(lobby));
    }
  }

  /**
   * Removes a user from the specified lobby.
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
        LOGGER.info("lobby removed after last member left: lobbyId={}",
            lobby.getLobbyId());
        return Optional.empty();
      }

      reassignHostIfNeededLocked(lobby, userId, members);
      lobby.setMembers(members);
      lobby.incrementVersion();
      return Optional.of(new Lobby(lobby));
    }
  }

  /**
   * Initiates the game start process for a lobby.
   */
  public Optional<GameStartOutcome> startGame(String lobbyId, String userId,
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
        LOGGER.warn("start rejected: lobbyId={}, userId={}, reason=not_member",
            lobby.getLobbyId(), userId);
        throw new SecurityException("Only lobby members can start the game");
      }

      if (lobby.isGameStarted() && lobby.getGameId() != null) {
        LOGGER.info("start accepted idempotent: lobbyId={}, userId={}, gameId={}",
            lobby.getLobbyId(), userId, lobby.getGameId());
        return Optional.of(new GameStartOutcome(new Lobby(lobby), lobby.getGameId(), false));
      }

      if (activeMembers.size() < 2) {
        LOGGER.warn("start rejected: lobbyId={}, userId={}, "
            + "reason=too_few_active_players, activeCount={}",
            lobby.getLobbyId(), userId, activeMembers.size());
        throw new IllegalStateException("At least 2 active players are required");
      }

      Lobby startSnapshot = new Lobby(lobby);
      startSnapshot.setMembers(activeMembers);
      String gameId = gameCreator.apply(startSnapshot);

      lobby.setMembers(activeMembers);
      lobby.setGameId(gameId);
      lobby.setGameStarted(true);
      lobby.incrementVersion();

      LOGGER.info("start accepted: lobbyId={}, userId={}, gameId={}, activeCount={}",
          lobby.getLobbyId(), userId, gameId, activeMembers.size());
      return Optional.of(new GameStartOutcome(new Lobby(lobby), gameId, true));
    }
  }

  /**
   * Marks a specific game as started within the lobby.
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
   * Retrieves a canonical copy of the given lobby.
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
   * Cleans up members who have timed out.
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
      LOGGER.info("stale member removed: lobbyId={}, userId={}, timeoutMs={}",
          lobby.getLobbyId(), removedId, memberTimeout.toMillis());
    }

    if (members.isEmpty()) {
      activeLobbies.remove(lobby.getLobbyId(), lobby);
      LOGGER.info("lobby removed after stale cleanup: lobbyId={}",
          lobby.getLobbyId());
      return;
    }

    if (removedIds.contains(lobby.getHostId())) {
      lobby.setHostId(members.getFirst().getId());
      LOGGER.info("host reassigned: lobbyId={}, newHostId={}, reason=host_expired",
          lobby.getLobbyId(), lobby.getHostId());
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
    LOGGER.info("host reassigned: lobbyId={}, newHostId={}, reason=host_left",
        lobby.getLobbyId(), lobby.getHostId());
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

  /**
   * Record containing the outcome of a game start.
   *
   * @param lobby   The updated lobby snapshot.
   * @param gameId  The assigned game ID.
   * @param created Whether a new game was created.
   */
  public record GameStartOutcome(Lobby lobby, String gameId, boolean created) {
    public GameStartOutcome {
      lobby = new Lobby(lobby);
    }

    @Override
    public Lobby lobby() {
      return new Lobby(lobby);
    }
  }
}

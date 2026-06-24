package com.doomhamsters.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests for the authoritative lobby service. */
class LobbyServiceTests {

  private LobbyService lobbyService;
  private User host;

  @BeforeEach
  void setUp() {
    lobbyService = new LobbyService(new QrCodeGeneratorService());
    host = new User("host-id", "Alice", "dog");
  }

  @Test
  void createLobbyReturnsAuthoritativeSnapshot() {
    Lobby lobby = lobbyService.createLobby("Room name", host);

    assertNotNull(lobby.getLobbyId());
    assertEquals("Room name", lobby.getGroupName());
    assertEquals("host-id", lobby.getHostId());
    assertEquals(1, lobby.getMembers().size());
    assertEquals("host-id", lobby.getMembers().get(0).getId());
    assertTrue(lobby.getMembers().get(0).isConnected());
    assertFalse(lobby.isGameStarted());
    assertNotNull(lobby.getQrCodeBase64());
  }

  @Test
  void fourPlayersJoinSequentially() {
    Lobby lobby = lobbyService.createLobby("Room", host);

    join(lobby, "p2");
    join(lobby, "p3");
    Lobby snapshot = join(lobby, "p4");

    assertEquals(List.of("host-id", "p2", "p3", "p4"), memberIds(snapshot));
  }

  @Test
  void fourPlayersJoinConcurrently() throws Exception {
    Lobby lobby = lobbyService.createLobby("Room", host);
    ExecutorService executor = Executors.newFixedThreadPool(3);
    CountDownLatch ready = new CountDownLatch(3);
    CountDownLatch start = new CountDownLatch(1);
    List<Callable<Lobby>> joins = List.of(
        concurrentJoin(lobby, "p2", ready, start),
        concurrentJoin(lobby, "p3", ready, start),
        concurrentJoin(lobby, "p4", ready, start));

    List<Future<Lobby>> futures = new ArrayList<>();
    joins.forEach(join -> futures.add(executor.submit(join)));
    ready.await();
    start.countDown();
    for (Future<Lobby> future : futures) {
      assertNotNull(future.get());
    }
    executor.shutdownNow();

    Lobby snapshot = lobbyService.getLobby(lobby.getLobbyId());
    assertEquals(4, snapshot.getMembers().size());
    assertEquals(Set.of("host-id", "p2", "p3", "p4"), Set.copyOf(memberIds(snapshot)));
  }

  @Test
  void duplicateJoinWithSamePlayerIdIsReconnectNotDuplicate() {
    Lobby lobby = lobbyService.createLobby("Room", host);

    join(lobby, "p2");
    Lobby reconnect = lobbyService.joinOrUpdateLobby(
        lobby.getLobbyId(),
        new User("p2", "New Bob", "fox")).orElseThrow();

    assertEquals(2, reconnect.getMembers().size());
    assertEquals("New Bob", reconnect.getMembers().get(1).getUsername());
    assertTrue(reconnect.getMembers().get(1).isConnected());
  }

  @Test
  void hostLeavesAndOldestRemainingMemberBecomesHost() {
    Lobby lobby = lobbyService.createLobby("Room", host);
    join(lobby, "p2");
    join(lobby, "p3");

    Lobby afterLeave = lobbyService.leaveLobby(lobby.getLobbyId(), "host-id").orElseThrow();

    assertEquals("p2", afterLeave.getHostId());
    assertEquals(List.of("p2", "p3"), memberIds(afterLeave));
  }

  @Test
  void anyActiveMemberCanStart() {
    Lobby lobby = lobbyService.createLobby("Room", host);
    join(lobby, "p2");

    LobbyService.GameStartOutcome outcome = lobbyService.startGame(
        lobby.getLobbyId(),
        "p2",
        ignored -> "game-1").orElseThrow();

    assertEquals("game-1", outcome.gameId());
    assertTrue(outcome.created());
  }

  @Test
  void doubleStartReturnsSameGameIdAndCreatesOneGame() {
    Lobby lobby = lobbyService.createLobby("Room", host);
    join(lobby, "p2");
    AtomicInteger createCount = new AtomicInteger();

    LobbyService.GameStartOutcome first = lobbyService.startGame(
        lobby.getLobbyId(),
        "host-id",
        ignored -> "game-" + createCount.incrementAndGet()).orElseThrow();
    LobbyService.GameStartOutcome second = lobbyService.startGame(
        lobby.getLobbyId(),
        "host-id",
        ignored -> "game-" + createCount.incrementAndGet()).orElseThrow();

    assertTrue(first.created());
    assertFalse(second.created());
    assertEquals("game-1", first.gameId());
    assertEquals("game-1", second.gameId());
    assertEquals(1, createCount.get());
  }

  @Test
  void startedLobbyRejectsNewJoins() {
    Lobby lobby = lobbyService.createLobby("Room", host);
    join(lobby, "p2");
    lobbyService.startGame(lobby.getLobbyId(), "host-id", ignored -> "game-1");

    String lobbyId = lobby.getLobbyId();
    User newPlayer = new User("p3", "P3", "cat");
    assertThrows(
        IllegalStateException.class,
        () -> lobbyService.joinOrUpdateLobby(lobbyId, newPlayer));
  }

  @Test
  void nonMemberStartIsRejected() {
    Lobby lobby = lobbyService.createLobby("Room", host);
    join(lobby, "p2");

    String lobbyId = lobby.getLobbyId();
    assertThrows(
        SecurityException.class,
        () -> lobbyService.startGame(lobbyId, "outsider", ignored -> "game-1"));
  }

  @Test
  void disconnectedPlayerCleanupRemovesExpiredMembersAndReassignsHost() {
    MutableClock clock = new MutableClock(Instant.parse("2026-05-17T12:00:00Z"));
    LobbyService service = new LobbyService(6, Duration.ofSeconds(5), clock, null, new QrCodeGeneratorService());
    Lobby lobby = service.createLobby("Room", host);
    service.joinOrUpdateLobby(lobby.getLobbyId(), new User("p2", "P2", "cat"));

    clock.advance(Duration.ofSeconds(3));
    service.heartbeat(lobby.getLobbyId(), "p2");
    clock.advance(Duration.ofSeconds(3));
    List<Lobby> changed = service.cleanupExpiredMembers();

    assertEquals(1, changed.size());
    Lobby snapshot = service.getLobby(lobby.getLobbyId());
    assertEquals(List.of("p2"), memberIds(snapshot));
    assertEquals("p2", snapshot.getHostId());
  }

  @Test
  void allPlayersReceiveSameGameIdInStartOutcomeAndLobbySnapshot() {
    Lobby lobby = lobbyService.createLobby("Room", host);
    join(lobby, "p2");
    join(lobby, "p3");
    join(lobby, "p4");

    LobbyService.GameStartOutcome outcome =
        lobbyService.startGame(lobby.getLobbyId(), "host-id", ignored -> "shared-game")
            .orElseThrow();
    Lobby snapshot = lobbyService.getLobby(lobby.getLobbyId());

    assertEquals("shared-game", outcome.gameId());
    assertEquals("shared-game", outcome.lobby().getGameId());
    assertEquals("shared-game", snapshot.getGameId());
    assertEquals(memberIds(outcome.lobby()), memberIds(snapshot));
  }

  @Test
  void lobbyIdsAreUniqueAndNotDerivedFromGroupName() {
    Lobby first = lobbyService.createLobby("Same Room", host);
    Lobby second = lobbyService.createLobby("Same Room", new User("host-2", "Bob", "cat"));

    assertNotEquals(first.getLobbyId(), second.getLobbyId());
    assertEquals("Same Room", first.getGroupName());
    assertEquals("Same Room", second.getGroupName());
  }

  @Test
  void operationsOnUnknownLobbyReturnEmpty() {
    assertTrue(lobbyService.joinOrUpdateLobby("unknown", host).isEmpty());
    assertTrue(lobbyService.heartbeat("unknown", "p1").isEmpty());
    assertTrue(lobbyService.leaveLobby("unknown", "p1").isEmpty());
    assertTrue(lobbyService.startGame("unknown", "p1", ignored -> "game-1").isEmpty());
    assertTrue(lobbyService.markGameStarted("unknown", "game-1").isEmpty());
    assertNull(lobbyService.getLobby("unknown"));
  }

  @Test
  void invalidUserIdsThrowExceptions() {
    assertThrows(IllegalArgumentException.class, () -> lobbyService.heartbeat("lobby-1", null));
    assertThrows(IllegalArgumentException.class, () -> lobbyService.heartbeat("lobby-1", "  "));
    assertThrows(IllegalArgumentException.class, () -> lobbyService.startGame("lobby-1", "", ignored -> "game"));
  }

  @Test
  void leaveLobbyWithNullArgumentsReturnsEmpty() {
    assertTrue(lobbyService.leaveLobby(null, "p1").isEmpty());
    assertTrue(lobbyService.leaveLobby("lobby-1", null).isEmpty());
  }

  @Test
  void lastMemberLeavingDestroysLobby() {
    Lobby lobby = lobbyService.createLobby("Room", host);

    // Host (and only player) leaves the room
    assertTrue(lobbyService.leaveLobby(lobby.getLobbyId(), "host-id").isEmpty());

    // The lobby should now be removed from the system
    assertNull(lobbyService.getLobby(lobby.getLobbyId()));
  }

  @Test
  void leaveLobbyDoesNothingIfMemberNotInLobby() {
    Lobby lobby = lobbyService.createLobby("Room", host);

    Lobby result = lobbyService.leaveLobby(lobby.getLobbyId(), "unknown-user").orElseThrow();

    // Host is still in the lobby
    assertEquals(1, result.getMembers().size());
  }

  @Test
  void heartbeatReturnsEmptyIfMemberNotInLobby() {
    Lobby lobby = lobbyService.createLobby("Room", host);
    assertTrue(lobbyService.heartbeat(lobby.getLobbyId(), "unknown-user").isEmpty());
  }

  @Test
  void startGameFailsWithNotEnoughPlayers() {
    Lobby lobby = lobbyService.createLobby("Room", host);

    // Only the host is inside (< 2 players) -> expected exception
    String lobbyId = lobby.getLobbyId();
    assertThrows(IllegalStateException.class, () ->
      lobbyService.startGame(lobbyId, "host-id", ignored -> "game-1")
    );
  }

  @Test
  void markGameStartedUpdatesLobbyState() {
    Lobby lobby = lobbyService.createLobby("Room", host);
    Lobby started = lobbyService.markGameStarted(lobby.getLobbyId(), "game-xyz").orElseThrow();

    assertTrue(started.isGameStarted());
    assertEquals("game-xyz", started.getGameId());
  }

  @Test
  void scheduledCleanupRunsWithoutErrors() {
    Lobby lobby = lobbyService.createLobby("Room", host);
    // Simulate the Spring scheduler
    lobbyService.scheduledCleanupExpiredMembers();

    assertNotNull(lobbyService.getLobby(lobby.getLobbyId()));
  }

  @Test
  void cleanupDestroysLobbyIfAllMembersExpire() {
    MutableClock clock = new MutableClock(Instant.parse("2026-05-17T12:00:00Z"));
    LobbyService service = new LobbyService(6, Duration.ofSeconds(5), clock, null, new QrCodeGeneratorService());
    Lobby lobby = service.createLobby("Room", host);

    // Advance time by 10 seconds so the timeout (5s) triggers
    clock.advance(Duration.ofSeconds(10));
    service.cleanupExpiredMembers();

    // Room should have been deleted
    assertNull(service.getLobby(lobby.getLobbyId()));
  }

  @Test
  void maxPlayersClampedToSix() {
    // Tests that the capacity cannot drop below 6 (Math.max(6, defaultMaxPlayers))
    LobbyService smallService = new LobbyService(1, Duration.ofMinutes(1), Clock.systemUTC(), null, new QrCodeGeneratorService());
    Lobby lobby = smallService.createLobby("Small Room", host);
    assertEquals(6, lobby.getMaxPlayers());
  }

  @Test
  void getLobbyIsCaseInsensitive() {
    Lobby lobby = lobbyService.createLobby("Room", host);
    String lowercaseId = lobby.getLobbyId().toLowerCase(Locale.ROOT);

    assertNotNull(lobbyService.getLobby(lowercaseId));
    assertEquals(lobby.getLobbyId(), lobbyService.getLobby(lowercaseId).getLobbyId());
  }

  @Test
  void expiredMembersNotRemovedFromStartedGame() {
    MutableClock clock = new MutableClock(Instant.parse("2026-05-17T12:00:00Z"));
    LobbyService service = new LobbyService(6, Duration.ofSeconds(5), clock, null, new QrCodeGeneratorService());

    Lobby lobby = service.createLobby("Room", host);
    service.joinOrUpdateLobby(lobby.getLobbyId(), new User("p2", "P2", "cat"));
    service.markGameStarted(lobby.getLobbyId(), "game-1");

    // Time expired
    clock.advance(Duration.ofSeconds(10));

    // In a started game, removeExpiredMembersLocked returns early
    Lobby snapshot = service.getLobby(lobby.getLobbyId());
    assertEquals(2, snapshot.getMembers().size());
  }

  @Test
  void createLobbyWithInvalidUserThrows() {
    assertThrows(IllegalArgumentException.class, () -> lobbyService.createLobby("Room", null));
    User invalidUser = new User("", "Name", "cat");
    assertThrows(IllegalArgumentException.class, () -> lobbyService.createLobby("Room", invalidUser));
  }

  @Test
  void joinOrUpdateLobbyWithInvalidUserThrows() {
    Lobby lobby = lobbyService.createLobby("Room", host);
    String lobbyId = lobby.getLobbyId();
    assertThrows(IllegalArgumentException.class, () -> lobbyService.joinOrUpdateLobby(lobbyId, null));
  }

  @Test
  void getLobbyExplicitlyWithNullReturnsNull() {
    assertNull(lobbyService.getLobby(null));
  }

  @Test
  void springAutowiredConstructorIsCovered() {
    // Covers the constructor called by Spring Boot during startup (ObjectProvider)
    @SuppressWarnings("unchecked")
    ObjectProvider<LobbyRealtimePublisher> providerMock = mock(ObjectProvider.class);
    when(providerMock.getIfAvailable()).thenReturn(null);

    LobbyService springService = new LobbyService(
      6,
      120000L,
      providerMock,
      new QrCodeGeneratorService()
    );

    assertNotNull(springService);
  }

  @Test
  void gameStartOutcomeRecordMethodsAreCovered() {
    // Covers the overridden lobby() method and the automatically generated record methods
    Lobby lobby = lobbyService.createLobby("Room", host);
    LobbyService.GameStartOutcome outcome = new LobbyService.GameStartOutcome(lobby, "game-record", true);

    assertNotNull(outcome.lobby());
    assertEquals("game-record", outcome.gameId());
    assertTrue(outcome.created());
  }

  @Test
  void scheduledCleanupWithRealtimePublisherBroadcastsSnapshots() {
    // Covers the 'else' branch in scheduledCleanup.
    LobbyRealtimePublisher mockPublisher = mock(LobbyRealtimePublisher.class);
    MutableClock clock = new MutableClock(Instant.parse("2026-05-17T12:00:00Z"));
    LobbyService service = new LobbyService(6, Duration.ofSeconds(5), clock, mockPublisher, new QrCodeGeneratorService());

    Lobby lobby = service.createLobby("Room", host);
    service.joinOrUpdateLobby(lobby.getLobbyId(), new User("p2", "P2", "cat"));

    // Advance time, host sends heartbeat, p2 expires
    clock.advance(Duration.ofSeconds(3));
    service.heartbeat(lobby.getLobbyId(), "host-id");
    clock.advance(Duration.ofSeconds(3));

    service.scheduledCleanupExpiredMembers();

    // Verifies that the code enters the for loop and invokes broadcast
    org.mockito.Mockito.verify(mockPublisher, org.mockito.Mockito.atLeastOnce())
      .broadcastLobbySnapshot(org.mockito.Mockito.any(Lobby.class));
  }

  @Test
  void cleanupExpiredMembersIgnoresNullLobbyInMap() throws Exception {
    java.lang.reflect.Field mapField = LobbyService.class.getDeclaredField("activeLobbies");
    mapField.setAccessible(true);
    java.util.Map<String, Lobby> spoofedMap = new java.util.HashMap<>();
    spoofedMap.put("SPOOF_ID", null);
    mapField.set(lobbyService, spoofedMap);

    List<Lobby> changed = lobbyService.cleanupExpiredMembers();
    assertTrue(changed.isEmpty());
  }

  @Test
  void isExpiredReturnsFalseIfLastSeenAtIsNull() {
    Lobby lobby = lobbyService.createLobby("Room", host);
    Lobby fetched = lobbyService.getLobby(lobby.getLobbyId());
    fetched.getMembers().get(0).markSeen(null);

    List<Lobby> changed = lobbyService.cleanupExpiredMembers();
    assertTrue(changed.isEmpty());
  }

  @Test
  void nonHostLeavingDoesNotReassignHost() {
    Lobby lobby = lobbyService.createLobby("Room", host);
    join(lobby, "p2");

    Lobby updated = lobbyService.leaveLobby(lobby.getLobbyId(), "p2").orElseThrow();

    // Host must remain "host-id"
    assertEquals("host-id", updated.getHostId());
  }

  private Lobby join(Lobby lobby, String playerId) {
    Optional<Lobby> joined = lobbyService.joinOrUpdateLobby(
        lobby.getLobbyId(),
        new User(playerId, "User " + playerId, "cat"));
    return joined.orElseThrow();
  }

  private Callable<Lobby> concurrentJoin(
      Lobby lobby,
      String playerId,
      CountDownLatch ready,
      CountDownLatch start) {
    return () -> {
      ready.countDown();
      start.await();
      return join(lobby, playerId);
    };
  }

  private List<String> memberIds(Lobby lobby) {
    return lobby.getMembers().stream()
        .map(User::getId)
        .toList();
  }

  private static final class MutableClock extends Clock {
    private Instant instant;

    private MutableClock(Instant instant) {
      this.instant = instant;
    }

    private void advance(Duration duration) {
      instant = instant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
      return ZoneId.of("UTC");
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return instant;
    }
  }
}

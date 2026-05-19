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

/** Tests for the authoritative lobby service. */
class LobbyServiceTests {

  private LobbyService lobbyService;
  private User host;

  @BeforeEach
  void setUp() {
    lobbyService = new LobbyService();
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

    assertThrows(
        IllegalStateException.class,
        () -> lobbyService.joinOrUpdateLobby(lobby.getLobbyId(), new User("p3", "P3", "cat")));
  }

  @Test
  void nonMemberStartIsRejected() {
    Lobby lobby = lobbyService.createLobby("Room", host);
    join(lobby, "p2");

    assertThrows(
        SecurityException.class,
        () -> lobbyService.startGame(lobby.getLobbyId(), "outsider", ignored -> "game-1"));
  }

  @Test
  void disconnectedPlayerCleanupRemovesExpiredMembersAndReassignsHost() {
    MutableClock clock = new MutableClock(Instant.parse("2026-05-17T12:00:00Z"));
    LobbyService service = new LobbyService(6, Duration.ofSeconds(5), clock, null);
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

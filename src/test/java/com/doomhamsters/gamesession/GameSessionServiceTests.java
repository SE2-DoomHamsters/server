package com.doomhamsters.gamesession;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.io.File;
import org.junit.jupiter.api.AfterEach;

class GameSessionServiceTests {

  private GameSessionService service;

  @BeforeEach
  void setUp() {
    service = new GameSessionService(new GameSessionPersistenceService());
  }

  @AfterEach
  void cleanup() {
    new File("sessions.json").delete();
  }

  @Test
  void testCreateSession() {
    String lobbyId = "lobby-1";
    GameSession session = service.createSession(lobbyId);

    assertNotNull(session);
    assertNotNull(session.getGameId());
    assertEquals(lobbyId, session.getLobbyId());

    // Prüfen, ob sie wirklich in der Map ist
    Optional<GameSession> retrieved = service.getSession(session.getGameId());
    assertTrue(retrieved.isPresent());
    assertEquals(session, retrieved.get());
  }

  @Test
  void testGetSessionNotFound() {
    Optional<GameSession> result = service.getSession("non-existent");
    assertTrue(result.isEmpty());
  }

  @Test
  void testSaveSession() {
    GameSession session = new GameSession("manual-id", "lobby-x");

    service.saveSession(session);

    Optional<GameSession> retrieved = service.getSession("manual-id");
    assertTrue(retrieved.isPresent());
    assertEquals("lobby-x", retrieved.get().getLobbyId());
  }

  @Test
  void testSessionsPersistAcrossServiceRestart() {

    GameSession created =
        service.createSession("persistent-lobby");

    GameSessionService restartedService =
        new GameSessionService(
            new GameSessionPersistenceService());

    Optional<GameSession> restored =
        restartedService.getSession(
            created.getGameId());

    assertTrue(restored.isPresent());

    assertEquals(
        "persistent-lobby",
        restored.get().getLobbyId());
  }
}

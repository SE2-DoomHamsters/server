package com.doomhamsters.gamesession;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;

class GameSessionServiceTests {

  private GameSessionService service;

  @BeforeEach
  void setUp() {
    service = new GameSessionService();
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
}

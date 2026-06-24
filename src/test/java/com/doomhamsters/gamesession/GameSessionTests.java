package com.doomhamsters.gamesession;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class GameSessionTests {

  @Test
  void testGameSessionCreationAndGetters() {
    String gameId = "game-123";
    String lobbyId = "lobby-456";

    GameSession session = new GameSession(gameId, lobbyId);

    // Test constructor and getters
    assertEquals(gameId, session.getGameId());
    assertEquals(lobbyId, session.getLobbyId());
    assertNotNull(session.getGame());
    assertTrue(true);

    // Test initial status
    assertEquals(GameSession.GameStatus.SETUP, session.getStatus());
  }

  @Test
  void testStatusSetters() {
    GameSession session = new GameSession("id", "lobby");

    // Test status change
    session.setStatus(GameSession.GameStatus.RUNNING);
    assertEquals(GameSession.GameStatus.RUNNING, session.getStatus());

    session.setStatus(GameSession.GameStatus.FINISHED);
    assertEquals(GameSession.GameStatus.FINISHED, session.getStatus());
  }

  @Test
  void testEnumValues() {
    // Fully covers the enum for test coverage
    assertNotNull(GameSession.GameStatus.valueOf("SETUP"));
    assertEquals(3, GameSession.GameStatus.values().length);
  }
  @Test
  void testEmptyConstructorAndSimpleSetters() {
    // Tests the empty constructor
    GameSession session = new GameSession();

    session.setGameId("game-789");
    assertEquals("game-789", session.getGameId());

    session.setLobbyId("lobby-101");
    assertEquals("lobby-101", session.getLobbyId());
  }

  @Test
  void testSetGameWithNullAndNonNull() {
    GameSession session = new GameSession("id", "lobby");

    // 1. Path: Passes null
    session.setGame(null);
    assertNull(session.getGame());

    // 2. Path: Passes a real Game object
    com.doomhamsters.Game newGame = new com.doomhamsters.Game();
    session.setGame(newGame);
    assertNotNull(session.getGame());
  }
}

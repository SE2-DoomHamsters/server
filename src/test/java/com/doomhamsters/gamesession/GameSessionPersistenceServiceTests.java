package com.doomhamsters.gamesession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class GameSessionPersistenceServiceTests {

  private static final String FILE_PATH = "sessions.json";

  @AfterEach
  void cleanup() {
    new File(FILE_PATH).delete();
  }

  @Test
  void saveAndLoadSessionsShouldWork() {

    GameSessionPersistenceService persistence =
        new GameSessionPersistenceService();

    ConcurrentHashMap<String, GameSession> sessions =
        new ConcurrentHashMap<>();

    GameSession session =
        new GameSession("game-1", "lobby-1");

    sessions.put(session.getGameId(), session);

    persistence.saveSessions(sessions);

    ConcurrentHashMap<String, GameSession> loaded =
        persistence.loadSessions();

    assertFalse(loaded.isEmpty());

    assertEquals(
        "game-1",
        loaded.get("game-1").getGameId());

    assertEquals(
        "lobby-1",
        loaded.get("game-1").getLobbyId());
  }
}
package com.doomhamsters.gamesession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

  @Test
  void loadSessionsShouldReturnEmptyMapWhenFileCannotBeDeserialized() throws IOException {

    Files.writeString(
        Path.of(FILE_PATH),
        "{invalid");

    GameSessionPersistenceService persistence =
        new GameSessionPersistenceService();

    assertTrue(
        persistence.loadSessions().isEmpty());
  }
}

package com.doomhamsters.gamesession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GameSessionPersistenceServiceTests {

  @TempDir
  File tempDir;

  @Test
  void saveAndLoadSessionsShouldWork() {
    String filePath = new File(tempDir, "sessions.json").getAbsolutePath();
    GameSessionPersistenceService persistence =
        new GameSessionPersistenceService(filePath);

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
    String filePath = new File(tempDir, "sessions.json").getAbsolutePath();

    Files.writeString(
        Path.of(filePath),
        "{invalid");

    GameSessionPersistenceService persistence =
        new GameSessionPersistenceService();

    assertTrue(
        persistence.loadSessions().isEmpty());
  }
}

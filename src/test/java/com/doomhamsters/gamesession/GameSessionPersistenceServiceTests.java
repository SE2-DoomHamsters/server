package com.doomhamsters.gamesession;

import com.doomhamsters.Card;
import com.doomhamsters.Game;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

    ConcurrentMap<String, GameSession> loaded =
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
        new GameSessionPersistenceService(filePath);

    assertTrue(
        persistence.loadSessions().isEmpty());
  }

  @Test
  void saveAndLoadRunningGameShouldRestoreBoardAndPlayers() {
    String filePath = new File(tempDir, "sessions.json").getAbsolutePath();
    GameSessionPersistenceService persistence =
        new GameSessionPersistenceService(filePath);

    GameSession session = new GameSession("game-1", "lobby-1");
    Game game = new Game();
    game.setupWithPlayers(
        List.of("p1", "p2"),
        List.of("Alpha", "Bravo"),
        createActionCards(),
        new Card("snack-proto", "Snack Stash", "snack_stash"),
        List.of(new Card("doom-1", "Doom Hamster", "doom")));
    String currentPlayerId = game.getBoard().getCurrentPlayer().getId();
    session.setGame(game);
    session.setStatus(GameSession.GameStatus.RUNNING);

    ConcurrentMap<String, GameSession> sessions = new ConcurrentHashMap<>();
    sessions.put(session.getGameId(), session);

    persistence.saveSessions(sessions);

    ConcurrentMap<String, GameSession> loaded = persistence.loadSessions();
    GameSession restored = loaded.get("game-1");

    assertFalse(loaded.isEmpty());
    assertEquals(GameSession.GameStatus.RUNNING, restored.getStatus());
    assertEquals(2, restored.getGame().getPlayers().size());
    assertEquals(currentPlayerId, restored.getGame().getBoard().getCurrentPlayer().getId());
  }

  private List<Card> createActionCards() {
    return List.of(
        new Card("act-1", "Action 1", "action"),
        new Card("act-2", "Action 2", "action"),
        new Card("act-3", "Action 3", "action"),
        new Card("act-4", "Action 4", "action"),
        new Card("act-5", "Action 5", "action"),
        new Card("act-6", "Action 6", "action"),
        new Card("act-7", "Action 7", "action"),
        new Card("act-8", "Action 8", "action"),
        new Card("act-9", "Action 9", "action"),
        new Card("act-10", "Action 10", "action"),
        new Card("act-11", "Action 11", "action"),
        new Card("act-12", "Action 12", "action"));
  }
}

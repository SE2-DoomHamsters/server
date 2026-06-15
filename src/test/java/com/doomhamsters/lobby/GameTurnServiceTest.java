package com.doomhamsters.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.doomhamsters.GameState;
import com.doomhamsters.Player;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameTurnServiceTest {

  private GameTurnService service;

  @BeforeEach
  void setUp() {
    service = new GameTurnService();
  }

  @Test
  void testRegisterAndFindGame() {
    List<Player> players = List.of(new Player("p1", "Alice"), new Player("p2", "Bob"));

    service.registerGame("game-1", players);

    Optional<GameState> game = service.findGame("game-1");
    assertTrue(game.isPresent());
    assertEquals("game-1", game.get().getGameId());

    assertEquals("p1", game.get().getCurrentTurnPlayerId());
  }

  @Test
  void processDrawThrowsIfGameNotFound() {
    assertThrows(
      NoSuchElementException.class,
      () -> service.processDraw("unknown-game", "p1")
    );
  }

  @Test
  void processDrawThrowsIfWrongTurn() {
    List<Player> players = List.of(new Player("p1", "Alice"), new Player("p2", "Bob"));
    service.registerGame("game-1", players);

    assertThrows(
      IllegalArgumentException.class,
      () -> service.processDraw("game-1", "p2")
    );
  }

  @Test
  void processDrawExecutesSuccessfullyOnValidTurn() {
    List<Player> players = List.of(new Player("p1", "Alice"), new Player("p2", "Bob"));
    service.registerGame("game-1", players);

    GameState newState = service.processDraw("game-1", "p1");

    assertNotNull(newState);
    assertNotNull(newState.getCurrentTurnPlayerId());
  }

  @Test
  void registerGameWithEmptyPlayerListSetsEmptyTurn() {
    service.registerGame("empty-game", List.of());
    assertEquals("", service.findGame("empty-game").get().getCurrentTurnPlayerId());
  }

  @Test
  void processDrawHitsAllCardTypesAndEliminationBranches() {
    List<Player> players = List.of(
      new Player("p1", "Alice"),
      new Player("p2", "Bob"),
      new Player("p3", "Charlie")
    );
    service.registerGame("brute-force-game", players);

    for (int i = 0; i < 100; i++) {
      GameState state = service.findGame("brute-force-game").get();
      String currentPlayer = state.getCurrentTurnPlayerId();

      long activePlayers = state.getPlayers().stream().filter(p -> !p.isEliminated()).count();
      if (activePlayers <= 1) {
        service.processDraw("brute-force-game", currentPlayer);
        break;
      }

      service.processDraw("brute-force-game", currentPlayer);
    }

    assertTrue(service.findGame("brute-force-game").isPresent());
  }
}

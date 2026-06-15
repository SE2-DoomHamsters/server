package com.doomhamsters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.doomhamsters.lobby.GameTurnService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link GameTurnService}.
 */
final class GameServiceTest {

  private static final String GAME_ID = "game-1";
  private static final String PLAYER_1 = "p1";
  private static final String PLAYER_2 = "p2";

  // 2. HIER GEÄNDERT
  private GameTurnService service;

  @BeforeEach
  void setUp() {
    // 3. HIER GEÄNDERT (Wie von dir vermutet)
    service = new GameTurnService();
  }

  // -------------------------------------------------------------------------
  // registerGame / findGame
  // -------------------------------------------------------------------------

  @Test
  void findGame_returnsEmptyForUnknownGame() {
    assertThat(service.findGame("unknown")).isEqualTo(Optional.empty());
  }

  @Test
  void findGame_returnsStateAfterRegistration() {
    service.registerGame(GAME_ID, GameStateTest.twoPlayers());
    assertThat(service.findGame(GAME_ID)).isPresent();
  }

  @Test
  void registerGame_setsFirstPlayerAsCurrentTurn() {
    service.registerGame(GAME_ID, GameStateTest.twoPlayers());
    GameState state = service.findGame(GAME_ID).orElseThrow();
    assertThat(state.getCurrentTurnPlayerId()).isEqualTo(PLAYER_1);
  }

  @Test
  void registerGame_withEmptyRoster_setsEmptyStringTurn() {
    service.registerGame(GAME_ID, List.of());
    GameState state = service.findGame(GAME_ID).orElseThrow();
    assertThat(state.getCurrentTurnPlayerId()).isEmpty();
  }

  @Test
  void registerGame_initialStateIsNotSnackStashPending() {
    service.registerGame(GAME_ID, GameStateTest.twoPlayers());
    GameState state = service.findGame(GAME_ID).orElseThrow();
    assertThat(state.isSnackStashPending()).isFalse();
  }

  // -------------------------------------------------------------------------
  // processDraw — validation
  // -------------------------------------------------------------------------

  @Test
  void processDraw_throwsWhenGameNotFound() {
    assertThrows(NoSuchElementException.class,
      () -> service.processDraw("nonexistent", PLAYER_1));
  }

  @Test
  void processDraw_throwsWhenNotPlayersTurn() {
    service.registerGame(GAME_ID, GameStateTest.twoPlayers());
    // p2 tries to draw, but it is p1's turn.
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> service.processDraw(GAME_ID, PLAYER_2));
    assertThat(exception.getMessage()).contains(PLAYER_2);
  }

  @Test
  void processDraw_doesNotMutateStateOnTurnViolation() {
    service.registerGame(GAME_ID, GameStateTest.twoPlayers());
    GameState before = service.findGame(GAME_ID).orElseThrow();
    try {
      service.processDraw(GAME_ID, PLAYER_2);
    } catch (IllegalArgumentException ignored) {
      // expected
    }
    GameState after = service.findGame(GAME_ID).orElseThrow();
    assertThat(after.getCurrentTurnPlayerId()).isEqualTo(before.getCurrentTurnPlayerId());
  }

  // -------------------------------------------------------------------------
  // processDraw — successful draw (property-based via repetition)
  // -------------------------------------------------------------------------

  @RepeatedTest(20)
  void processDraw_alwaysReturnsUpdatedState() {
    service.registerGame(GAME_ID, GameStateTest.twoPlayers());
    GameState updated = service.processDraw(GAME_ID, PLAYER_1);
    assertThat(updated).isNotNull();
    assertThat(updated.getGameId()).isEqualTo(GAME_ID);
  }
  }

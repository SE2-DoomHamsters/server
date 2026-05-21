package com.doomhamsters;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link DrawError}. */
final class DrawErrorTest{

  private static final String GAME_ID = "game-42";
  private static final String PLAYER_ID = "player-1";
  private static final String REASON = "Not your turn";

  @Test
  void getGameId_returnsValuePassedToConstructor() {
    DrawError error = new DrawError(GAME_ID, PLAYER_ID, REASON);
    assertThat(error.getGameId()).isEqualTo(GAME_ID);
  }

  @Test
  void getPlayerId_returnsValuePassedToConstructor() {
    DrawError error = new DrawError(GAME_ID, PLAYER_ID, REASON);
    assertThat(error.getPlayerId()).isEqualTo(PLAYER_ID);
  }

  @Test
  void getReason_returnsValuePassedToConstructor() {
    DrawError error = new DrawError(GAME_ID, PLAYER_ID, REASON);
    assertThat(error.getReason()).isEqualTo(REASON);
  }

  @Test
  void allFieldsPreservedTogether() {
    DrawError error = new DrawError("g1", "p9", "Game not found: g1");
    assertThat(error.getGameId()).isEqualTo("g1");
    assertThat(error.getPlayerId()).isEqualTo("p9");
    assertThat(error.getReason()).isEqualTo("Game not found: g1");
  }
}

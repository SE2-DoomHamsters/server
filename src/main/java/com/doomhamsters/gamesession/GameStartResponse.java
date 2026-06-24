package com.doomhamsters.gamesession;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Simple DTO to signal that a game has been started
 * and to provide its ID.
 */
@Schema(description = "Response returned when a game is successfully started")
public class GameStartResponse {

  @Schema(description = "Unique game session identifier — use this to subscribe to "
      + "/topic/game/{gameId} for real-time game state updates",
      example = "550e8400-e29b-41d4-a716-446655440000")
  private final String gameId;

  /**
   * Constructor for GameStartResponse.
   *
   * @param gameId the generated ID of the game
   */
  public GameStartResponse(String gameId) {
    this.gameId = gameId;
  }

  /**
   * Returns the game ID.
   *
   * @return the ID of the game
   */
  public String getGameId() {
    return gameId;
  }
}

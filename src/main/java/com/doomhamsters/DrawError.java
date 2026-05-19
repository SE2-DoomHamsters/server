package com.doomhamsters;

import io.swagger.v3.oas.annotations.media.Schema;

/** Error response sent to the requesting client on a failed draw attempt. */
@Schema(description = "Error response returned when a draw action fails")
public final class DrawError {

  @Schema(
      description = "ID of the game where the error occurred",
      example = "GAME_001"
  )
  private final String gameId;

  @Schema(
      description = "ID of the player who initiated the failed request",
      example = "player-123"
  )
  private final String playerId;

  @Schema(
      description = "Human-readable explanation of the failure",
      example = "Not player player-123's turn"
  )
  private final String reason;

  /** Constructor for the default DrawError. */
  public DrawError(String gameId, String playerId, String reason) {
    this.gameId = gameId;
    this.playerId = playerId;
    this.reason = reason;
  }

  public String getGameId() {
    return gameId;
  }

  public String getPlayerId() {
    return playerId;
  }

  public String getReason() {
    return reason;
  }
}


package com.doomhamsters;

import io.swagger.v3.oas.annotations.media.Schema;

/** Payload sent by the client when requesting to draw a card. */
@Schema(description = "Request payload for drawing a card")
public final class DrawRequest {

  @Schema(
      description = "ID of the player requesting to draw a card",
      example = "player-123"
  )
  private String playerId;

  /**DrawRequest empty Constructor. */
  public DrawRequest() {}

  /**DrawRequest Constructor for playerId.*/
  public DrawRequest(String playerId) {
    this.playerId = playerId;
  }

  public String getPlayerId() {
    return playerId;
  }

  public void setPlayerId(String playerId) {
    this.playerId = playerId;
  }

}


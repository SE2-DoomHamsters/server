package com.doomhamsters;

/** Error response sent to the requesting client on a failed draw attempt. */

public final class DrawError {

  private final String gameId;
  private final String playerId;
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


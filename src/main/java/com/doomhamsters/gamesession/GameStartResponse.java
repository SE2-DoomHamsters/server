package com.doomhamsters.gamesession;

/**
 * simple DTO to signal that a game has started and provide its ID.
 */
public class GameStartResponse {
  private final String gameId;

  public GameStartResponse(String gameId) {
    this.gameId = gameId;
  }

  public String getGameId() {
    return gameId;
  }
}

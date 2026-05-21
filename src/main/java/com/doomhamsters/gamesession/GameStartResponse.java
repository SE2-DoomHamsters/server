package com.doomhamsters.gamesession;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Einfaches DTO, um zu signalisieren, dass ein Spiel gestartet
 * wurde, und dessen ID bereitzustellen.
 */
@Schema(description = "Response returned when a game is successfully started")
public class GameStartResponse {

  @Schema(description = "Unique game session identifier — use this to subscribe to "
      + "/topic/game/{gameId} for real-time game state updates",
      example = "550e8400-e29b-41d4-a716-446655440000")
  private final String gameId;

  /**
   * Konstruktor für die GameStartResponse.
   *
   * @param gameId Die generierte ID des Spiels
   */
  public GameStartResponse(String gameId) {
    this.gameId = gameId;
  }

  /**
   * Gibt die Spiel-ID zurück.
   *
   * @return Die ID des Spiels
   */
  public String getGameId() {
    return gameId;
  }
}

package com.doomhamsters.gamesession.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing the visible game state for a client.
 */
public class GameStateDto {

  private String gameId;

  private String gameState;

  private String currentPlayerId;

  private int turnCount;

  private List<PlayerStateDto> players = new ArrayList<>();

  public GameStateDto() {
  }

  public String getGameId() {
    return gameId;
  }

  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  public String getGameState() {
    return gameState;
  }

  public void setGameState(String gameState) {
    this.gameState = gameState;
  }

  public String getCurrentPlayerId() {
    return currentPlayerId;
  }

  public void setCurrentPlayerId(String currentPlayerId) {
    this.currentPlayerId = currentPlayerId;
  }

  public int getTurnCount() {
    return turnCount;
  }

  public void setTurnCount(int turnCount) {
    this.turnCount = turnCount;
  }

  public List<PlayerStateDto> getPlayers() {
    return new ArrayList<>(players);
  }

  /**
   * Sets the visible players in the game state.
   *
   * @param players the player DTO list
   */
  public void setPlayers(List<PlayerStateDto> players) {
    this.players = players == null
        ? new ArrayList<>()
        : new ArrayList<>(players);
  }
}
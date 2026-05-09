package com.doomhamsters.gamesession;

import com.doomhamsters.Game;

public class GameSession {
  private final String gameId;
  private final String lobbyId;
  private final Game game;
  private GameStatus status;

  public GameSession(String gameId, String lobbyId) {
    this.gameId = gameId;
    this.lobbyId = lobbyId;
    this.game = new Game();
    this.status = GameStatus.SETUP;
  }

  public String getGameId() { return gameId; }
  public String getLobbyId() { return lobbyId; }
  public Game getGame() { return game; }

  public GameStatus getStatus() { return status; }
  public void setStatus(GameStatus status) { this.status = status; }

  public enum GameStatus { SETUP, RUNNING, FINISHED }
}

package com.doomhamsters.gamesession;

import com.doomhamsters.Game;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;

/**
 * Represents a single game session and its current state.
 */
public class GameSession {

  private String gameId;
  private String lobbyId;
  private Game game;
  private GameStatus status;

  /**
   * Empty constructor required for JSON deserialization.
   */
  public GameSession() {
  }

  /**
   * Constructs a new GameSession.
   *
   * @param gameId the unique identifier for the game
   * @param lobbyId the identifier of the lobby this game started from
   */
  public GameSession(String gameId, String lobbyId) {
    this.gameId = gameId;
    this.lobbyId = lobbyId;
    this.game = new Game();
    this.status = GameStatus.SETUP;
  }

  /**
   * Gets the game ID.
   *
   * @return the game ID
   */
  public String getGameId() {
    return gameId;
  }

  /**
   * Sets the game ID.
   *
   * @param gameId the game ID
   */
  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  /**
   * Gets the lobby ID.
   *
   * @return the lobby ID
   */
  public String getLobbyId() {
    return lobbyId;
  }

  /**
   * Sets the lobby ID.
   *
   * @param lobbyId the lobby ID
   */
  public void setLobbyId(String lobbyId) {
    this.lobbyId = lobbyId;
  }

  /**
   * Gets the internal game logic instance.
   *
   * @return the game instance
   */
  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Game getGame() {
    return game;
  }

  /**
   * Sets the game instance.
   *
   * @param game the game instance
   */
  public void setGame(Game game) {
    this.game = game;
  }

  /**
   * Gets the current status of the game session.
   *
   * @return the game status
   */
  public GameStatus getStatus() {
    return status;
  }

  /**
   * Sets the new status of the game session.
   *
   * @param status the new status
   */
  public void setStatus(GameStatus status) {
    this.status = status;
  }

  /**
   * Possible states for a game session.
   */
  public enum GameStatus {
    SETUP,
    RUNNING,
    FINISHED
  }
}
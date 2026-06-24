package com.doomhamsters.gamesession;

import com.doomhamsters.Game;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;

/**
 * Represents a single game session and its current status.
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
   * Creates a new GameSession.
   *
   * @param gameId the unique identifier for the game
   * @param lobbyId the identifier of the lobby from which this game was started
   */
  public GameSession(String gameId, String lobbyId) {
    this.gameId = gameId;
    this.lobbyId = lobbyId;
    this.game = new Game();
    this.status = GameStatus.SETUP;
  }

  /**
   * Returns the game ID.
   *
   * @return the ID of the game
   */
  public String getGameId() {
    return gameId;
  }

  /**
   * Sets the game ID.
   *
   * @param gameId the game ID to set
   */
  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  /**
   * Returns the lobby ID.
   *
   * @return the ID of the lobby
   */
  public String getLobbyId() {
    return lobbyId;
  }

  /**
   * Sets the lobby ID.
   *
   * @param lobbyId the lobby ID to set
   */
  public void setLobbyId(String lobbyId) {
    this.lobbyId = lobbyId;
  }

  /**
   * Returns the internal game logic instance.
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
   * <p>If null is passed, the instance is set to null,
   * otherwise a copy of the passed instance is stored.
   *
   * @param game the game instance to set
   */
  public void setGame(Game game) {
    this.game = game == null
        ? null
        : new Game(game);
  }

  /**
   * Returns the current status of the game session.
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
    /** The game is currently being set up and has not started yet. */
    SETUP,
    /** The game is actively running. */
    RUNNING,
    /** The game has finished and a winner has been determined. */
    FINISHED
  }
}

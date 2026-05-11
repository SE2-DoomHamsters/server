package com.doomhamsters.gamesession;

import com.doomhamsters.Game;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;

/**
 * Represents a single game session and its current state.
 */
public class GameSession {
  private final String gameId;
  private final String lobbyId;
  private final Game game;
  private GameStatus status;

  /**
   * Constructs a new GameSession.
   *
   * @param gameId  the unique identifier for the game
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
   * Gets the lobby ID.
   *
   * @return the lobby ID
   */
  public String getLobbyId() {
    return lobbyId;
  }

  /**
   * Gets the internal game logic instance.
   *
   * @return the game instance (never null)
   */
  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Game getGame() {
    return game;
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
    SETUP, RUNNING, FINISHED
  }
}

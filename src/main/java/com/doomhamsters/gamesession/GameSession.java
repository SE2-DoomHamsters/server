package com.doomhamsters.gamesession;

import com.doomhamsters.Game;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;

/**
 * Repräsentiert eine einzelne Spielsitzung und ihren aktuellen Status.
 */
public class GameSession {

  private String gameId;
  private String lobbyId;
  private Game game;
  private GameStatus status;

  /**
   * Leerer Konstruktor, der für die JSON-Deserialisierung benötigt wird.
   */
  public GameSession() {
  }

  /**
   * Erstellt eine neue GameSession.
   *
   * @param gameId Die eindeutige Kennung für das Spiel
   * @param lobbyId Die Kennung der Lobby, aus der dieses Spiel gestartet wurde
   */
  public GameSession(String gameId, String lobbyId) {
    this.gameId = gameId;
    this.lobbyId = lobbyId;
    this.game = new Game();
    this.status = GameStatus.SETUP;
  }

  /**
   * Gibt die Spiel-ID zurück.
   *
   * @return Die ID des Spiels
   */
  public String getGameId() {
    return gameId;
  }

  /**
   * Setzt die Spiel-ID.
   *
   * @param gameId Die zu setzende Spiel-ID
   */
  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  /**
   * Gibt die Lobby-ID zurück.
   *
   * @return Die ID der Lobby
   */
  public String getLobbyId() {
    return lobbyId;
  }

  /**
   * Setzt die Lobby-ID.
   *
   * @param lobbyId Die zu setzende Lobby-ID
   */
  public void setLobbyId(String lobbyId) {
    this.lobbyId = lobbyId;
  }

  /**
   * Gibt die interne Spiellogik-Instanz zurück.
   *
   * @return Die Spiel-Instanz
   */
  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Game getGame() {
    return game;
  }

  /**
   * Setzt die Spiel-Instanz.
   *
   * <p>Wird null übergeben, wird die Instanz auf null gesetzt,
   * ansonsten wird eine Kopie der übergebenen Instanz gespeichert.
   *
   * @param game Die zu setzende Spiel-Instanz
   */
  public void setGame(Game game) {
    this.game = game == null
        ? null
        : new Game(game);
  }

  /**
   * Gibt den aktuellen Status der Spielsitzung zurück.
   *
   * @return Der Spielstatus
   */
  public GameStatus getStatus() {
    return status;
  }

  /**
   * Setzt den neuen Status der Spielsitzung.
   *
   * @param status Der neue Status
   */
  public void setStatus(GameStatus status) {
    this.status = status;
  }

  /**
   * Mögliche Zustände für eine Spielsitzung.
   */
  public enum GameStatus {
    /** Das Spiel wird gerade eingerichtet und ist noch nicht gestartet. */
    SETUP,
    /** Das Spiel läuft aktiv. */
    RUNNING,
    /** Das Spiel wurde beendet und ein Gewinner steht fest. */
    FINISHED
  }
}

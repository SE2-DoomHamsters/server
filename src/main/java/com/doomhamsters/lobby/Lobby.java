package com.doomhamsters.lobby;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

/**
 * Container für eine Spiel-Lobby.
 */
public class Lobby {
  @Schema(description = "Die eindeutige ID der Lobby", example = "MEINE_LOBBY")
  private String lobbyId;
  @Schema(description = "Display name of the lobby group", example = "DoomHamsters")
  private String groupName;
  private String hostId;
  private int maxPlayers = 6;
  @Schema(description = "Liste aller aktiven Mitglieder in der Lobby")
  private List<User> members = new ArrayList<>();
  @Schema(description = "Der Base64-codierte String des Lobby-QR-Codes", example = "iVBORw0KGgo...")
  private String qrCodeBase64;
  @Schema(description = "Game session id after the lobby starts a game",
      example = "550e8400-e29b-41d4-a716-446655440000")
  private String gameId;
  @Schema(description = "Whether the lobby has started a game", example = "true")
  private boolean gameStarted;
  @Schema(description = "Monotonically increasing lobby snapshot version", example = "3")
  private int version = 0;

  /**
   * Standard-Konstruktor für Frameworks und Deserialisierung.
   */
  public Lobby() {}

  /**
   * Erstellt eine neue Lobby mit der angegebenen ID.
   *
   * @param lobbyId Die eindeutige ID der Lobby
   */
  public Lobby(String lobbyId) {
    this.lobbyId = lobbyId;
  }

  /**
   * Creates a defensive copy of another lobby.
   *
   * @param other lobby to copy
   */
  public Lobby(Lobby other) {
    this.lobbyId = other.lobbyId;
    this.groupName = other.groupName;
    this.hostId = other.hostId;
    this.maxPlayers = other.maxPlayers;
    this.members = other.getMembers();
    this.qrCodeBase64 = other.qrCodeBase64;
    this.gameId = other.gameId;
    this.gameStarted = other.gameStarted;
    this.version = other.version;
  }

  /**
   * Gibt die ID der Lobby zurück.
   *
   * @return Die ID der Lobby
   */
  public String getLobbyId() {
    return lobbyId;
  }

  /**
   * Setzt die ID der Lobby.
   *
   * @param lobbyId Die zu setzende Lobby-ID
   */
  public void setLobbyId(String lobbyId) {
    this.lobbyId = lobbyId;
  }

  /**
   * Returns the lobby group display name.
   *
   * @return group display name
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * Sets the lobby group display name.
   *
   * @param groupName group display name
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  /**
   * Returns the current lobby host id.
   *
   * @return host user id
   */
  @JsonIgnore
  public String getHostId() {
    return hostId;
  }

  /**
   * Sets the current lobby host id.
   *
   * @param hostId host user id
   */
  public void setHostId(String hostId) {
    this.hostId = hostId;
  }

  /**
   * Returns the configured maximum number of players.
   *
   * @return maximum player count
   */
  @JsonIgnore
  public int getMaxPlayers() {
    return maxPlayers;
  }

  /**
   * Sets the configured maximum number of players.
   *
   * @param maxPlayers maximum player count
   */
  public void setMaxPlayers(int maxPlayers) {
    this.maxPlayers = maxPlayers;
  }

  /**
   * Returns the monotonically increasing lobby snapshot version.
   *
   * @return lobby version
   */
  public int getVersion() {
    return version;
  }

  /** Increments the lobby snapshot version after an authoritative state change. */
  public void incrementVersion() {
    this.version++;
  }

  /**
   * Gibt eine Kopie der Mitgliederliste zurück.
   *
   * @return Eine Liste aller Benutzer in dieser Lobby
   */
  public List<User> getMembers() {
    if (members == null) {
      return new ArrayList<>();
    }

    List<User> snapshot = new ArrayList<>();
    members.stream()
        .map(User::new)
        .forEach(snapshot::add);
    return snapshot;
  }

  /**
   * Überschreibt die Liste der Mitglieder mit einer neuen Liste.
   *
   * @param members Die neue Liste der Benutzer
   */
  // FIX für EI_EXPOSE_REP2: Speichert eine Kopie, statt die Referenz direkt zu übernehmen
  public void setMembers(List<User> members) {
    this.members = new ArrayList<>();
    if (members != null) {
      members.stream()
          .map(User::new)
          .forEach(this.members::add);
    }
  }

  /**
   * Gibt den generierten QR-Code als Base64-String zurück.
   *
   * @return Der QR-Code als Base64-String
   */
  public String getQrCodeBase64() {
    return qrCodeBase64;
  }

  /**
   * Setzt den QR-Code als Base64-String.
   *
   * @param qrCodeBase64 Der zu setzende Base64-String
   */
  public void setQrCodeBase64(String qrCodeBase64) {
    this.qrCodeBase64 = qrCodeBase64;
  }

  /**
   * Returns the started game id.
   *
   * @return game id, or {@code null} before the game starts
   */
  public String getGameId() {
    return gameId;
  }

  /**
   * Sets the started game id.
   *
   * @param gameId game id
   */
  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  /**
   * Returns whether a game has started from this lobby.
   *
   * @return {@code true} after the lobby starts a game
   */
  public boolean isGameStarted() {
    return gameStarted;
  }

  /**
   * Sets whether a game has started from this lobby.
   *
   * @param gameStarted game started flag
   */
  public void setGameStarted(boolean gameStarted) {
    this.gameStarted = gameStarted;
  }
}

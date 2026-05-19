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
  private String groupName;
  private String hostId;
  private int maxPlayers = 6;
  @Schema(description = "Liste aller aktiven Mitglieder in der Lobby")
  private List<User> members = new ArrayList<>();
  @Schema(description = "Der Base64-codierte String des Lobby-QR-Codes", example = "iVBORw0KGgo...")
  private String qrCodeBase64;
  private String gameId;
  private boolean gameStarted;
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

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  @JsonIgnore
  public String getHostId() {
    return hostId;
  }

  public void setHostId(String hostId) {
    this.hostId = hostId;
  }

  @JsonIgnore
  public int getMaxPlayers() {
    return maxPlayers;
  }

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
   * Returns a defensive copy of lobby members.
   *
   * @return member snapshot
   */
  /**
   * Gibt eine Kopie der Mitgliederliste zurück.
   *
   * @return Eine Liste aller Benutzer in dieser Lobby
   */
  // FIX für EI_EXPOSE_REP: Gibt eine Kopie zurück, statt das Original
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

  public String getGameId() {
    return gameId;
  }

  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  public boolean isGameStarted() {
    return gameStarted;
  }

  public void setGameStarted(boolean gameStarted) {
    this.gameStarted = gameStarted;
  }
}

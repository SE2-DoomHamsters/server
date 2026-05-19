package com.doomhamsters.lobby;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

/**
 * Container für eine Spiel-Lobby.
 */
public class Lobby {
  @Schema(description = "Die eindeutige ID der Lobby", example = "MEINE_LOBBY")
  private String lobbyId;
  @Schema(description = "Liste aller aktiven Mitglieder in der Lobby")
  private List<User> members = new ArrayList<>();
  @Schema(description = "Der Base64-codierte String des Lobby-QR-Codes", example = "iVBORw0KGgo...")
  private String qrCodeBase64;

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
   * Gibt eine Kopie der Mitgliederliste zurück.
   *
   * @return Eine Liste aller Benutzer in dieser Lobby
   */
  // FIX für EI_EXPOSE_REP: Gibt eine Kopie zurück, statt das Original
  public List<User> getMembers() {
    return (members == null) ? null : new ArrayList<>(members);
  }

  /**
   * Überschreibt die Liste der Mitglieder mit einer neuen Liste.
   *
   * @param members Die neue Liste der Benutzer
   */
  // FIX für EI_EXPOSE_REP2: Speichert eine Kopie, statt die Referenz direkt zu übernehmen
  public void setMembers(List<User> members) {
    this.members = (members == null) ? new ArrayList<>() : new ArrayList<>(members);
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
}

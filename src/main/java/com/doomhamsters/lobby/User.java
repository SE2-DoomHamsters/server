package com.doomhamsters.lobby;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Objects;

/**
 * Repräsentiert einen Benutzer in einer Lobby.
 */
public class User {
  @Schema(description = "Die eindeutige Socket-Session-ID des Benutzers", example = "abc-12345")
  private String id;
  @Schema(description = "Der gewählte Anzeigename des Benutzers", example = "DoomSlayer")
  private String username;
  @Schema(description = "Das gewählte Icon oder Emoji des Benutzers", example = "🐹")
  private String avatar;
  private boolean connected = true;

  @JsonIgnore
  private Instant lastSeenAt = Instant.now();

  /**
   * Standard-Konstruktor für Frameworks.
   */
  public User() {}

  /**
   * Erstellt einen neuen User mit allen Details.
   *
   * @param id Die eindeutige ID (z.B. Socket-Session).
   * @param username Der gewählte Anzeigename.
   * @param avatar Das gewählte Icon/Emoji.
   */
  public User(String id, String username, String avatar) {
    this.id = id;
    this.username = username;
    this.avatar = avatar;
    this.connected = true;
    this.lastSeenAt = Instant.now();
  }

  /**
   * Creates a defensive copy of another user.
   *
   * @param other user to copy
   */
  public User(User other) {
    this.id = other.id;
    this.username = other.username;
    this.avatar = other.avatar;
    this.connected = other.connected;
    this.lastSeenAt = other.lastSeenAt;
  }

  /**
   * Gibt die ID des Users zurück.
   *
   * @return Die ID des Users.
   */
  public String getId() {
    return id;
  }

  /**
   * Setzt die ID des Users.
   *
   * @param id Die zu setzende ID.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gibt den Namen des Users zurück.
   *
   * @return Der Name des Users.
   */
  public String getUsername() {
    return username;
  }

  /**
   * Setzt den Namen des Users.
   *
   * @param username Der zu setzende Name.
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Gibt den gewählten Avatar zurück.
   *
   * @return Der gewählte Avatar.
   */
  public String getAvatar() {
    return avatar;
  }

  /**
   * Setzt den Avatar des Users.
   *
   * @param avatar Der zu setzende Avatar.
   */
  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  public boolean isConnected() {
    return connected;
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  @JsonIgnore
  public Instant getLastSeenAt() {
    return lastSeenAt;
  }

  public void setLastSeenAt(Instant lastSeenAt) {
    this.lastSeenAt = lastSeenAt;
  }

  /** Marks this lobby member as connected right now. */
  public void markSeen(Instant seenAt) {
    this.connected = true;
    this.lastSeenAt = seenAt;
  }

  /**
   * Vergleicht diesen User mit einem anderen Objekt auf Gleichheit anhand der ID.
   *
   * @param o Das zu vergleichende Objekt
   * @return true, wenn die IDs übereinstimmen, sonst false
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(id, user.id);
  }

  /**
   * Generiert den Hashcode basierend auf der User-ID.
   *
   * @return Der generierte Hashcode
   */
  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}

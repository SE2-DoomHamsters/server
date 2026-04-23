package com.doomhamsters.lobby;

import java.util.Objects;

/**
 * Repräsentiert einen Benutzer in einer Lobby.
 */
public class User {
  private String id;
  private String username;
  private String avatar;

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

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}

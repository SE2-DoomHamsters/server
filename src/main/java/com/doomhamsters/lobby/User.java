package com.doomhamsters.lobby;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a user in a lobby.
 */
public class User {
  @Schema(description = "The unique socket session ID of the user", example = "abc-12345")
  private String id;
  @Schema(description = "The chosen display name of the user", example = "DoomSlayer")
  private String username;
  @Schema(description = "The chosen icon or emoji of the user", example = "🐹")
  private String avatar;
  @Schema(description = "Whether the user is currently connected", example = "true")
  private boolean connected = true;

  @JsonIgnore
  private Instant lastSeenAt = Instant.now();

  /**
   * Default constructor for frameworks.
   */
  public User() {}

  /**
   * Creates a new user with all details.
   *
   * @param id the unique ID (e.g., socket session)
   * @param username the chosen display name
   * @param avatar the chosen icon/emoji
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
   * Returns the ID of the user.
   *
   * @return the ID of the user
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the ID of the user.
   *
   * @param id the ID to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns the username of the user.
   *
   * @return the username of the user
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the username of the user.
   *
   * @param username the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Returns the chosen avatar.
   *
   * @return the chosen avatar
   */
  public String getAvatar() {
    return avatar;
  }

  /**
   * Sets the avatar of the user.
   *
   * @param avatar the avatar to set
   */
  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  /**
   * Returns whether the user is currently connected.
   *
   * @return {@code true} when the user is connected
   */
  public boolean isConnected() {
    return connected;
  }

  /**
   * Sets whether the user is currently connected.
   *
   * @param connected connected flag
   */
  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  /**
   * Returns when the user was last seen.
   *
   * @return last seen timestamp
   */
  @JsonIgnore
  public Instant getLastSeenAt() {
    return lastSeenAt;
  }

  /**
   * Sets when the user was last seen.
   *
   * @param lastSeenAt last seen timestamp
   */
  public void setLastSeenAt(Instant lastSeenAt) {
    this.lastSeenAt = lastSeenAt;
  }

  /**
   * Marks this lobby member as connected at the supplied timestamp.
   *
   * @param seenAt timestamp to store as last seen
   */
  public void markSeen(Instant seenAt) {
    this.connected = true;
    this.lastSeenAt = seenAt;
  }

  /**
   * Compares this user with another object for equality based on the ID.
   *
   * @param o the object to compare
   * @return true if the IDs match, otherwise false
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
   * Generates the hash code based on the user ID.
   *
   * @return the generated hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}

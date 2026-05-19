package com.doomhamsters.lobby;

import io.swagger.v3.oas.annotations.media.Schema;

/** DTO for the create-lobby REST request. */
@Schema(description = "Request body for creating a new lobby")
public class CreateLobbyRequest {

  @Schema(description = "Display name of the group/lobby", example = "DoomHamsters")
  private String groupName;

  @Schema(description = "The player creating the lobby")
  private User user;

  /**
   * Creates an empty create-lobby request.
   */
  public CreateLobbyRequest() {
    // Required by Jackson when binding JSON request bodies.
  }

  /**
   * Returns the requested lobby group name.
   *
   * @return group name
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * Sets the requested lobby group name.
   *
   * @param groupName group name
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  // Defensive copy – avoids EI_EXPOSE_REP (User is mutable)
  /**
   * Returns the user creating the lobby.
   *
   * @return defensive user copy, or {@code null}
   */
  public User getUser() {
    return user == null ? null : new User(user.getId(), user.getUsername(), user.getAvatar());
  }

  // Defensive copy – avoids EI_EXPOSE_REP2
  /**
   * Sets the user creating the lobby.
   *
   * @param user creator user
   */
  public void setUser(User user) {
    this.user = user == null ? null : new User(user.getId(), user.getUsername(), user.getAvatar());
  }
}

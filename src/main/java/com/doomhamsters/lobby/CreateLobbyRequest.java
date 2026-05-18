package com.doomhamsters.lobby;

import io.swagger.v3.oas.annotations.media.Schema;

/** DTO for the create-lobby REST request. */
@Schema(description = "Request body for creating a new lobby")
public class CreateLobbyRequest {

  @Schema(description = "Display name of the group/lobby", example = "DoomHamsters")
  private String groupName;

  @Schema(description = "The player creating the lobby")
  private User user;

  public CreateLobbyRequest() {}

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  // Defensive copy – avoids EI_EXPOSE_REP (User is mutable)
  public User getUser() {
    return user == null ? null : new User(user.getId(), user.getUsername(), user.getAvatar());
  }

  // Defensive copy – avoids EI_EXPOSE_REP2
  public void setUser(User user) {
    this.user = user == null ? null : new User(user.getId(), user.getUsername(), user.getAvatar());
  }
}

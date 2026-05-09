package com.doomhamsters.lobby;

/** DTO for the create-lobby REST request. */
public class CreateLobbyRequest {

  private String groupName;
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

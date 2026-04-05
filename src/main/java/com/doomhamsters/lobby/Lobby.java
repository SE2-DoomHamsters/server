package com.doomhamsters.lobby;

import java.util.ArrayList;
import java.util.List;

/**
 * Container für eine Spiel-Lobby.
 */
public class Lobby {
  private String lobbyId;
  private List<User> members = new ArrayList<>();
  private String qrCodeBase64;

  public Lobby() {}

  public Lobby(String lobbyId) {
    this.lobbyId = lobbyId;
  }

  public String getLobbyId() {
    return lobbyId;
  }

  public void setLobbyId(String lobbyId) {
    this.lobbyId = lobbyId;
  }

  public List<User> getMembers() {
    return members;
  }

  public void setMembers(List<User> members) {
    this.members = members;
  }

  public String getQrCodeBase64() {
    return qrCodeBase64;
  }

  public void setQrCodeBase64(String qrCodeBase64) {
    this.qrCodeBase64 = qrCodeBase64;
  }
}

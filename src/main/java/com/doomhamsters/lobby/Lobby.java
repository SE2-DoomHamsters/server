package com.doomhamsters.lobby;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

/**
 * Container für eine Spiel-Lobby.
 */
public class Lobby {
  private String lobbyId;
  private String groupName;
  private String hostId;
  private int maxPlayers = 6;
  private List<User> members = new ArrayList<>();
  private String qrCodeBase64;
  private String gameId;
  private boolean gameStarted;
  private int version = 0;

  public Lobby() {}

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

  public String getLobbyId() {
    return lobbyId;
  }

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
   * Replaces lobby members with defensive copies.
   *
   * @param members new members
   */
  public void setMembers(List<User> members) {
    this.members = new ArrayList<>();
    if (members != null) {
      members.stream()
          .map(User::new)
          .forEach(this.members::add);
    }
  }

  public String getQrCodeBase64() {
    return qrCodeBase64;
  }

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

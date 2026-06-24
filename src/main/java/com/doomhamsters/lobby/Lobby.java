package com.doomhamsters.lobby;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for a game lobby.
 */
public class Lobby {
  @Schema(description = "The unique ID of the lobby", example = "MY_LOBBY")
  private String lobbyId;
  @Schema(description = "Display name of the lobby group", example = "DoomHamsters")
  private String groupName;
  private String hostId;
  private int maxPlayers = 6;
  @Schema(description = "List of all active members in the lobby")
  private List<User> members = new ArrayList<>();
  @Schema(description = "The Base64-encoded string of the lobby QR code",
      example = "iVBORw0KGgo...")
  private String qrCodeBase64;
  @Schema(description = "Game session id after the lobby starts a game",
      example = "550e8400-e29b-41d4-a716-446655440000")
  private String gameId;
  @Schema(description = "Whether the lobby has started a game", example = "true")
  private boolean gameStarted;
  @Schema(description = "Monotonically increasing lobby snapshot version", example = "3")
  private int version = 0;

  /**
   * Default constructor for frameworks and deserialization.
   */
  public Lobby() {}

  /**
   * Creates a new lobby with the specified ID.
   *
   * @param lobbyId the unique ID of the lobby
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

  /**
   * Returns the ID of the lobby.
   *
   * @return the ID of the lobby
   */
  public String getLobbyId() {
    return lobbyId;
  }

  /**
   * Sets the ID of the lobby.
   *
   * @param lobbyId the lobby ID to set
   */
  public void setLobbyId(String lobbyId) {
    this.lobbyId = lobbyId;
  }

  /**
   * Returns the lobby group display name.
   *
   * @return group display name
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * Sets the lobby group display name.
   *
   * @param groupName group display name
   */
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  /**
   * Returns the current lobby host id.
   *
   * @return host user id
   */
  @JsonIgnore
  public String getHostId() {
    return hostId;
  }

  /**
   * Sets the current lobby host id.
   *
   * @param hostId host user id
   */
  public void setHostId(String hostId) {
    this.hostId = hostId;
  }

  /**
   * Returns the configured maximum number of players.
   *
   * @return maximum player count
   */
  @JsonIgnore
  public int getMaxPlayers() {
    return maxPlayers;
  }

  /**
   * Sets the configured maximum number of players.
   *
   * @param maxPlayers maximum player count
   */
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
   * Returns a copy of the member list.
   *
   * @return a list of all users in this lobby
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
   * Overwrites the list of members with a new list.
   *
   * @param members the new list of users
   */
  // FIX für EI_EXPOSE_REP2: Stores a copy instead of directly accepting the reference
  public void setMembers(List<User> members) {
    this.members = new ArrayList<>();
    if (members != null) {
      members.stream()
          .map(User::new)
          .forEach(this.members::add);
    }
  }

  /**
   * Returns the generated QR code as a Base64 string.
   *
   * @return the QR code as a Base64 string
   */
  public String getQrCodeBase64() {
    return qrCodeBase64;
  }

  /**
   * Sets the QR code as a Base64 string.
   *
   * @param qrCodeBase64 the Base64 string to set
   */
  public void setQrCodeBase64(String qrCodeBase64) {
    this.qrCodeBase64 = qrCodeBase64;
  }

  /**
   * Returns the started game id.
   *
   * @return game id, or {@code null} before the game starts
   */
  public String getGameId() {
    return gameId;
  }

  /**
   * Sets the started game id.
   *
   * @param gameId game id
   */
  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  /**
   * Returns whether a game has started from this lobby.
   *
   * @return {@code true} after the lobby starts a game
   */
  public boolean isGameStarted() {
    return gameStarted;
  }

  /**
   * Sets whether a game has started from this lobby.
   *
   * @param gameStarted game started flag
   */
  public void setGameStarted(boolean gameStarted) {
    this.gameStarted = gameStarted;
  }
}

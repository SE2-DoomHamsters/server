package com.doomhamsters;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;



/**
 * Snapshot of the full game at a point in time.
 *
 * <p>Broadcast to {@code /topic/game/{gameId}} after every state change.
 */
@Schema(description = "Represents the current state of an active game")
public class GameState {

  @Schema(
      description = "Unique identifier of the game session",
      example = "GAME_001"
  )
  private final String gameId;

  @Schema(
      description = "List of players currently participating in the game"
  )
  private final List<Player> players;

  @Schema(
      description = "ID of the player whose turn is currently active",
      example = "player-123"
  )
  private final String currentTurnPlayerId;

  /** Indicates a Snack Stash card is pending client confirmation for {@code pendingPlayerId}. */
  @Schema(
      description = "Whether a Snack Stash card effect is awaiting confirmation",
      example = "false"
  )
  private final boolean snackStashPending;

  @Schema(
      description = "Player currently required to confirm the Snack Stash effect",
      example = "player-123",
      nullable = true
  )
  private final String pendingPlayerId;

  /** Constructor for the GameState. */
  public GameState(
      String gameId,
      List<Player> players,
      String currentTurnPlayerId,
      boolean snackStashPending,
      String pendingPlayerId) {
    this.gameId = gameId;
    this.players = List.copyOf(players);
    this.currentTurnPlayerId = currentTurnPlayerId;
    this.snackStashPending = snackStashPending;
    this.pendingPlayerId = pendingPlayerId;
  }

  public String getGameId() {
    return gameId;
  }

  public List<Player> getPlayers() {
    return players;
  }

  public String getCurrentTurnPlayerId() {
    return currentTurnPlayerId;
  }

  public boolean isSnackStashPending() {
    return snackStashPending;
  }

  public String getPendingPlayerId() {
    return pendingPlayerId;
  }

}


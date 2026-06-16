package com.doomhamsters.gamesession.dto;

import com.doomhamsters.gamesession.snackstash.SnackStashClaimEventDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO representing the visible game state for a client.
 */
@Schema(description = "Filtered game state visible to one requesting player")
@Getter
@Setter
public class GameStateDto {

  @Schema(description = "Unique game session identifier",
      example = "550e8400-e29b-41d4-a716-446655440000")
  private String gameId;

  @Schema(description = "Current game lifecycle state", example = "RUNNING")
  private String gameState;

  @Schema(description = "ID of the player whose turn is active", example = "player-1")
  private String currentPlayerId;

  @Schema(description = "ID of the player resolving a Doom card, if any", example = "player-1")
  private String resolvingDoomPlayerId;

  @Schema(description = "Whether the player must insert a neutralized Doom card", example = "true")
  private boolean pendingDoomRequiresInsertion;

  @Schema(description = "ID of the neutralized Doom card waiting for insertion", example = "doom_0")
  private String pendingDoomCardId;

  @Schema(description = "Pending Snack Stash claim awaiting votes, if any")
  private SnackStashClaimEventDto pendingSnackStashClaim;

  @Schema(description = "Number of cards left in the draw deck", example = "31")
  private int remainingDeckSize;

  @Schema(description = "Number of completed turns", example = "4")
  private int turnCount;

  @Schema(description = "Players in turn order with hands filtered for the requester")
  private List<PlayerStateDto> players = new ArrayList<>();

  /**
   * Creates an empty game state DTO.
   */
  public GameStateDto() {
    // Required by Jackson for response body serialization.
  }

  /**
   * Returns the visible players in the game state.
   *
   * @return defensive player DTO list
   */
  public List<PlayerStateDto> getPlayers() {
    return new ArrayList<>(players);
  }

  /**
   * Sets the visible players in the game state.
   *
   * @param players the player DTO list
   */
  public void setPlayers(List<PlayerStateDto> players) {
    this.players = players == null
        ? new ArrayList<>()
        : new ArrayList<>(players);
  }
}

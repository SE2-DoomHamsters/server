package com.doomhamsters.gamesession.snackstash;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Public event emitted when a Doom-resolving player claims a selected card as Snack Stash.
 */
@Schema(description = "Public WebSocket event for a pending Snack Stash claim")
@Getter
public class SnackStashClaimEventDto {

  private static final String TYPE = "SNACK_STASH_CLAIM_PENDING";

  @Schema(description = "Unique claim identifier", example = "claim-123")
  @Setter
  private String claimId;
  @Schema(description = "ID of the player claiming to use Snack Stash", example = "player-1")
  @Setter
  private String playerId;
  @Schema(description = "Display name of the claiming player", example = "Alice")
  @Setter
  private String playerName;
  @Schema(description = "Number of eligible votes required to resolve the claim", example = "2")
  @Setter
  private int votesRequired;
  @Schema(description = "Number of votes already submitted", example = "1")
  @Setter
  private int votesReceived;
  @Schema(description = "IDs of players who have already voted")
  private List<String> votedPlayerIds = List.of();
  @Schema(description = "Public claim message", example = "Alice claims Snack Stash.")
  @Setter
  private String message;

  /**
   * Creates an empty event for STOMP serialization.
   */
  public SnackStashClaimEventDto() {
    // Required by the STOMP/Jackson serializer when materializing message payloads.
  }

  /**
   * Returns the event type.
   *
   * @return event type
   */
  public String getType() {
    return TYPE;
  }

  /**
   * Sets the ids of players who have voted.
   *
   * @param votedPlayerIds voted player ids
   */
  public void setVotedPlayerIds(List<String> votedPlayerIds) {
    this.votedPlayerIds = votedPlayerIds == null ? List.of() : List.copyOf(votedPlayerIds);
  }
}

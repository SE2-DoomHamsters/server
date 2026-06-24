package com.doomhamsters.gamesession.snackstash;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;

/**
 * Public event emitted when a Snack Stash claim resolves.
 */
@Schema(description = "Public WebSocket event emitted after a Snack Stash claim resolves")
@Getter
public class SnackStashResolutionEventDto {

  private static final String TYPE = "SNACK_STASH_RESOLVED";

  @Schema(description = "Resolution outcome", example = "CHEATER")
  private final String outcome;
  @Schema(description = "Resolved claim identifier", example = "claim-123")
  private final String claimId;
  @Schema(description = "ID of the player whose Snack Stash claim resolved", example = "player-1")
  private final String claimingPlayerId;
  @Schema(description = "Display name of the claiming player", example = "Alice")
  private final String claimingPlayerName;
  @Schema(description = "IDs of players who voted NO against the claim")
  private final List<String> accusingPlayerIds;
  @Schema(description = "Life total changes caused by the resolution")
  private final List<SnackStashLifeChange> lifeChanges;
  @Schema(description = "ID of the first affected player for legacy clients", example = "player-1")
  private final String affectedPlayerId;
  @Schema(description = "Display name of the first affected player", example = "Alice")
  private final String affectedPlayerName;
  @Schema(description = "First affected player's lives before resolution", example = "3")
  private final int livesBefore;
  @Schema(description = "First affected player's lives after resolution", example = "2")
  private final int livesAfter;
  @Schema(description = "Whether the pending Doom card was defused", example = "false")
  private final boolean doomDefused;
  @Schema(description = "Public resolution message",
      example = "Alice was caught cheating and loses 1 life.")
  private final String message;

  /**
   * Creates an event from a domain resolution.
   *
   * @param resolution resolved domain result
   */
  public SnackStashResolutionEventDto(SnackStashResolution resolution) {
    this.outcome = resolution.getOutcome().name();
    this.claimId = resolution.getClaimId();
    this.claimingPlayerId = resolution.getClaimingPlayerId();
    this.claimingPlayerName = resolution.getClaimingPlayerName();
    this.accusingPlayerIds = resolution.getAccusingPlayerIds() == null
        ? List.of()
        : List.copyOf(resolution.getAccusingPlayerIds());
    this.lifeChanges = resolution.getLifeChanges() == null
        ? List.of()
        : List.copyOf(resolution.getLifeChanges());
    SnackStashLifeChange firstLifeChange =
        this.lifeChanges.isEmpty() ? null : this.lifeChanges.getFirst();
    this.affectedPlayerId = firstLifeChange == null ? null : firstLifeChange.getPlayerId();
    this.affectedPlayerName = firstLifeChange == null ? null : firstLifeChange.getPlayerName();
    this.livesBefore = firstLifeChange == null ? 0 : firstLifeChange.getLivesBefore();
    this.livesAfter = firstLifeChange == null ? 0 : firstLifeChange.getLivesAfter();
    this.doomDefused = resolution.isDoomDefused();
    this.message = resolution.getMessage();
  }

  /**
   * Returns the event type.
   *
   * @return event type
   */
  public String getType() {
    return TYPE;
  }

}

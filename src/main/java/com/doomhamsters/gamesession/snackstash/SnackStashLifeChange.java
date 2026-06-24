package com.doomhamsters.gamesession.snackstash;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Life total delta caused by a resolved Snack Stash claim.
 */
@Schema(description = "One player's life total change after a Snack Stash resolution")
@Getter
public class SnackStashLifeChange {

  @Schema(description = "Affected player identifier", example = "player-1")
  private final String playerId;
  @Schema(description = "Affected player display name", example = "Alice")
  private final String playerName;
  @Schema(description = "Life total before the resolution", example = "3")
  private final int livesBefore;
  @Schema(description = "Life total after the resolution", example = "2")
  private final int livesAfter;

  /**
   * Creates one life change entry.
   *
   * @param playerId affected player id
   * @param playerName affected player display name
   * @param livesBefore lives before resolution
   * @param livesAfter lives after resolution
   */
  public SnackStashLifeChange(
      String playerId,
      String playerName,
      int livesBefore,
      int livesAfter) {

    this.playerId = playerId;
    this.playerName = playerName;
    this.livesBefore = livesBefore;
    this.livesAfter = livesAfter;
  }
}

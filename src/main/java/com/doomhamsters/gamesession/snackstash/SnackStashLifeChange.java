package com.doomhamsters.gamesession.snackstash;

import lombok.Getter;

/**
 * Life total delta caused by a resolved Snack Stash claim.
 */
@Getter
public class SnackStashLifeChange {

  private final String playerId;
  private final String playerName;
  private final int livesBefore;
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

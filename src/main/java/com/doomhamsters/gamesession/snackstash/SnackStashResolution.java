package com.doomhamsters.gamesession.snackstash;

import java.util.List;
import lombok.Getter;

/**
 * Domain result produced after a Snack Stash claim is resolved.
 */
@Getter
public class SnackStashResolution {

  private final SnackStashOutcome outcome;
  private final String claimId;
  private final String claimingPlayerId;
  private final String claimingPlayerName;
  private final List<String> accusingPlayerIds;
  private final List<SnackStashLifeChange> lifeChanges;
  private final boolean doomDefused;
  private final String message;

  private SnackStashResolution(Builder builder) {
    this.outcome = builder.outcome;
    this.claimId = builder.claimId;
    this.claimingPlayerId = builder.claimingPlayerId;
    this.claimingPlayerName = builder.claimingPlayerName;
    this.accusingPlayerIds = List.copyOf(builder.accusingPlayerIds);
    this.lifeChanges = List.copyOf(builder.lifeChanges);
    this.doomDefused = builder.doomDefused;
    this.message = builder.message;
  }

  static Builder builder(
      SnackStashOutcome outcome,
      String claimId) {

    return new Builder(outcome, claimId);
  }

  static final class Builder {

    private final SnackStashOutcome outcome;
    private final String claimId;
    private String claimingPlayerId;
    private String claimingPlayerName;
    private List<String> accusingPlayerIds = List.of();
    private List<SnackStashLifeChange> lifeChanges = List.of();
    private boolean doomDefused;
    private String message;

    private Builder(
        SnackStashOutcome outcome,
        String claimId) {

      this.outcome = outcome;
      this.claimId = claimId;
    }

    Builder claimingPlayer(
        String claimingPlayerId,
        String claimingPlayerName) {

      this.claimingPlayerId = claimingPlayerId;
      this.claimingPlayerName = claimingPlayerName;
      return this;
    }

    Builder accusingPlayerIds(List<String> accusingPlayerIds) {
      this.accusingPlayerIds =
          accusingPlayerIds == null ? List.of() : List.copyOf(accusingPlayerIds);
      return this;
    }

    Builder lifeChanges(List<SnackStashLifeChange> lifeChanges) {
      this.lifeChanges = lifeChanges == null ? List.of() : List.copyOf(lifeChanges);
      return this;
    }

    Builder doomDefused(boolean doomDefused) {
      this.doomDefused = doomDefused;
      return this;
    }

    Builder message(String message) {
      this.message = message;
      return this;
    }

    SnackStashResolution build() {
      return new SnackStashResolution(this);
    }
  }
}

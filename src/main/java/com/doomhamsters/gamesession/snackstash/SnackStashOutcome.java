package com.doomhamsters.gamesession.snackstash;

/**
 * Public outcome of a Snack Stash claim after all required votes are submitted.
 */
public enum SnackStashOutcome {
  /** Every voter accepted the claim. */
  UNCHALLENGED,

  /** The claimed card was not Snack Stash and the claimant lost a life. */
  CHEATER,

  /** One or more NO votes challenged a real Snack Stash. */
  LEGITIMATE_CALL
}

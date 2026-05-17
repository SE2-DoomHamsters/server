package com.doomhamsters.gamesession.cardcommands;

/**
 * Executes one authoritative card effect.
 */
public interface CardCommand {

  /**
   * Applies the command effect to the game.
   *
   * @param context execution context
   * @return command result
   */
  CardCommandResult execute(CardCommandContext context);
}

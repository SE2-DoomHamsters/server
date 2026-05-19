package com.doomhamsters.gamesession.cardcommands;

import com.doomhamsters.Card;

/**
 * Self-contained definition for one playable card.
 */
public interface CardDefinition extends CardCommand {

  /**
   * Returns the frontend card type id.
   *
   * @return card type id
   */
  String cardType();

  /**
   * Returns the command or effect id used by clients.
   *
   * @return command id
   */
  String commandId();

  /**
   * Returns the card display name.
   *
   * @return display name
   */
  String displayName();

  /**
   * Returns the effect id sent in card DTOs.
   *
   * @return effect id
   */
  default String effectId() {
    return commandId();
  }

  /**
   * Creates this card for a player's testing starting hand.
   *
   * @param playerId owning player id
   * @return card instance
   */
  Card createTestingCard(String playerId);
}

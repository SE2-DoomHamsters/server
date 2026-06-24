package com.doomhamsters.gamesession.cardcommands;

import com.doomhamsters.Card;

/**
 * Self-contained definition for one playable card.
 */
public interface CardDefinition extends CardCommand {

  /**
   * Extracts a required string parameter from the context.
   *
   * @param context execution context
   * @param key parameter name
   * @return non-blank string value
   * @throws IllegalArgumentException if the parameter is absent or blank
   */
  default String requiredParam(CardCommandContext context, String key) {
    Object value = context.getParameters().get(key);
    if (value == null || value.toString().isBlank()) {
      throw new IllegalArgumentException(commandId() + ": missing required parameter: " + key);
    }
    return value.toString();
  }

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
   * Returns whether this command can be undone by Squick.
   *
   * <p>All cards return {@code true} except Squick itself.
   *
   * @return {@code false} only for Squick
   */
  default boolean isUndoable() {
    return true;
  }

  /**
   * Creates this card for a player's testing starting hand.
   *
   * <p>The default implementation derives the card id from {@link #commandId()} converted to
   * lower-case, e.g. {@code "two_hamsters_p1"} for command id {@code "TWO_HAMSTERS"}.
   *
   * @param playerId owning player id
   * @return card instance
   */
  default Card createTestingCard(String playerId) {
    return new Card(commandId().toLowerCase() + "_" + playerId, displayName(), cardType());
  }
}

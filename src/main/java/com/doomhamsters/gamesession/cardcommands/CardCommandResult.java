package com.doomhamsters.gamesession.cardcommands;

import com.doomhamsters.Card;

/**
 * Describes events produced by a card command.
 */
public class CardCommandResult {

  private final String publicMessage;
  private final String privateMessage;
  private final Card revealedCard;

  private CardCommandResult(
      String publicMessage,
      String privateMessage,
      Card revealedCard) {

    this.publicMessage = publicMessage;
    this.privateMessage = privateMessage;
    this.revealedCard = revealedCard == null ? null : new Card(revealedCard);
  }

  /**
   * Creates a result with only a public message.
   *
   * @param publicMessage message visible to all players
   * @return card command result
   */
  public static CardCommandResult publicOnly(String publicMessage) {
    return new CardCommandResult(publicMessage, null, null);
  }

  /**
   * Creates a result that includes private information for the acting player.
   *
   * @param publicMessage message visible to all players
   * @param privateMessage message visible only to the acting player
   * @param revealedCard card revealed only to the acting player
   * @return card command result
   */
  public static CardCommandResult withPrivateResult(
      String publicMessage,
      String privateMessage,
      Card revealedCard) {

    return new CardCommandResult(publicMessage, privateMessage, revealedCard);
  }

  /**
   * Returns the public message.
   *
   * @return message visible to all players
   */
  public String getPublicMessage() {
    return publicMessage;
  }

  /**
   * Returns whether the result includes private information.
   *
   * @return {@code true} when private text or a revealed card is present
   */
  public boolean hasPrivateResult() {
    return privateMessage != null || revealedCard != null;
  }

  /**
   * Returns the private message.
   *
   * @return private message, or {@code null}
   */
  public String getPrivateMessage() {
    return privateMessage;
  }

  /**
   * Returns the privately revealed card.
   *
   * @return defensive card copy, or {@code null}
   */
  public Card getRevealedCard() {
    return revealedCard == null ? null : new Card(revealedCard);
  }
}

package com.doomhamsters.gamesession.cardcommands;

import com.doomhamsters.Card;
import java.util.Collections;
import java.util.List;

/**
 * Describes events produced by a card command.
 */
public class CardCommandResult {

  private final String publicMessage;
  private final String privateMessage;
  private final Card revealedCard;
  private final List<Card> revealedCards;

  private CardCommandResult(
      String publicMessage,
      String privateMessage,
      Card revealedCard,
      List<Card> revealedCards) {

    this.publicMessage = publicMessage;
    this.privateMessage = privateMessage;
    this.revealedCard = revealedCard == null ? null : new Card(revealedCard);
    this.revealedCards = (revealedCards == null || revealedCards.isEmpty())
        ? Collections.emptyList()
        : revealedCards.stream().map(Card::new).toList();
  }

  /**
   * Creates a result with only a public message.
   *
   * @param publicMessage message visible to all players
   * @return card command result
   */
  public static CardCommandResult publicOnly(String publicMessage) {
    return new CardCommandResult(publicMessage, null, null, null);
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

    return new CardCommandResult(publicMessage, privateMessage, revealedCard, null);
  }

  /**
   * Creates a result that privately reveals multiple cards to the acting player, in deck order.
   *
   * @param publicMessage message visible to all players
   * @param privateMessage message visible only to the acting player
   * @param revealedCards cards revealed only to the acting player, in deck order
   * @return card command result
   */
  public static CardCommandResult withMultipleRevealedCards(
      String publicMessage,
      String privateMessage,
      List<Card> revealedCards) {

    return new CardCommandResult(publicMessage, privateMessage, null, revealedCards);
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
   * @return {@code true} when private text, a revealed card, or revealed cards are present
   */
  public boolean hasPrivateResult() {
    return privateMessage != null || revealedCard != null || !revealedCards.isEmpty();
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

  /**
   * Returns the privately revealed cards in deck order.
   *
   * @return defensive copies of revealed cards; empty when none
   */
  public List<Card> getRevealedCards() {
    return revealedCards.stream().map(Card::new).toList();
  }
}

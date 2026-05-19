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

  public static CardCommandResult publicOnly(String publicMessage) {
    return new CardCommandResult(publicMessage, null, null);
  }

  public static CardCommandResult withPrivateResult(
      String publicMessage,
      String privateMessage,
      Card revealedCard) {

    return new CardCommandResult(publicMessage, privateMessage, revealedCard);
  }

  public String getPublicMessage() {
    return publicMessage;
  }

  public boolean hasPrivateResult() {
    return privateMessage != null || revealedCard != null;
  }

  public String getPrivateMessage() {
    return privateMessage;
  }

  public Card getRevealedCard() {
    return revealedCard == null ? null : new Card(revealedCard);
  }
}

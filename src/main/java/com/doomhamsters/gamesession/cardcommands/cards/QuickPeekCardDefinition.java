package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.Deck;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import com.doomhamsters.gamesession.cardcommands.CardDefinition;
import org.springframework.stereotype.Component;

/**
 * Card definition for Quick Peek.
 */
@Component
public class QuickPeekCardDefinition implements CardDefinition {

  /** Creates the Quick Peek card definition. */
  public QuickPeekCardDefinition() {}

  @Override
  public String cardType() {
    return "QuickPeek";
  }

  @Override
  public String commandId() {
    return "QUICK_PEEK";
  }

  @Override
  public String displayName() {
    return "Quick Peek";
  }

  @Override
  public Card createTestingCard(String playerId) {
    return new Card("quick_peek_" + playerId, displayName(), cardType());
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    Card topCard = topCard(context.getGame().getDeck());
    String privateMessage = topCard == null
        ? "The deck is empty."
        : "Top card: " + topCard.getName() + ".";

    return CardCommandResult.withPrivateResult(
        context.getPlayer().getName() + " activated Quick Peek.",
        privateMessage,
        topCard);
  }

  private Card topCard(Deck deck) {
    if (deck == null || deck.getCards().isEmpty()) {
      return null;
    }

    return new Card(deck.getCards().get(0));
  }
}

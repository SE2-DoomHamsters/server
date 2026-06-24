package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.Deck;
import com.doomhamsters.gamesession.cardcommands.AbstractCardDefinition;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import org.springframework.stereotype.Component;

/**
 * Card definition for Quick Peek.
 */
@Component
public class QuickPeekCardDefinition extends AbstractCardDefinition {

  public QuickPeekCardDefinition() {
    super("QuickPeek", "QUICK_PEEK", "Quick Peek");
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

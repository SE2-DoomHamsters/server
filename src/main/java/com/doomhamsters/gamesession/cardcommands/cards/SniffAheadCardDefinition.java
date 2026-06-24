package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.Deck;
import com.doomhamsters.gamesession.cardcommands.AbstractCardDefinition;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Card definition for Sniff Ahead.
 *
 * <p>Privately sends the top 3 deck cards (or fewer if the deck is smaller) to the playing
 * player. The deck order is not changed.
 */
@Component
public class SniffAheadCardDefinition extends AbstractCardDefinition {

  public SniffAheadCardDefinition() {
    super("SniffAhead", "SNIFF_AHEAD", "Sniff Ahead");
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    Deck deck = context.getGame().getDeck();
    List<Card> topCards = deck.getCards().stream()
        .limit(3)
        .toList();

    String privateMessage;
    if (topCards.isEmpty()) {
      privateMessage = "The deck is empty.";
    } else {
      List<String> names = topCards.stream().map(Card::getName).toList();
      privateMessage = "Top " + topCards.size() + " card(s): "
          + String.join(", ", names) + ".";
    }

    return CardCommandResult.withMultipleRevealedCards(
        context.getPlayer().getName() + " activated Sniff Ahead.",
        privateMessage,
        topCards);
  }
}

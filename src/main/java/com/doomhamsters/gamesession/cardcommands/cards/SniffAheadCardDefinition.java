package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.Deck;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import com.doomhamsters.gamesession.cardcommands.CardDefinition;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Card definition for Sniff Ahead.
 *
 * <p>Privately sends the top 3 deck cards (or fewer if the deck is smaller) to the playing
 * player. The deck order is not changed.
 */
@Component
public class SniffAheadCardDefinition implements CardDefinition {

  public SniffAheadCardDefinition() {
  }

  @Override
  public String cardType() {
    return "SniffAhead";
  }

  @Override
  public String commandId() {
    return "SNIFF_AHEAD";
  }

  @Override
  public String displayName() {
    return "Sniff Ahead";
  }

  @Override
  public Card createTestingCard(String playerId) {
    return new Card(
        "sniff_ahead_" + playerId,
        displayName(),
        cardType());
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

package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.AbstractCardDefinition;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Card definition for playing 4 identical Hamsters to steal 1 life.
 */
@Component
public class FourHamstersCardDefinition extends AbstractCardDefinition {

  private static final int REQUIRED_COUNT = 4;

  public FourHamstersCardDefinition() {
    super("HAMSTER_FOUR", "FOUR_HAMSTERS", "Hamster Combo: 4-of-a-Kind");
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    String targetPlayerId = requiredParam(context, "targetPlayerId");
    String hamsterType = requiredParam(context, "hamsterType");

    if (!hamsterType.startsWith("hamster_")) {
      throw new IllegalArgumentException(
        "FourHamsters: hamsterType must start with 'hamster_', got: " + hamsterType);
    }

    Player requester = context.getPlayer();
    Player target = context.getGame().getPlayers().stream()
        .filter(p -> p.getId().equals(targetPlayerId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
        "FourHamsters: unknown targetPlayerId=" + targetPlayerId));

    if (target.getId().equals(requester.getId())) {
      throw new IllegalArgumentException("FourHamsters: cannot target yourself");
    }

    if (target.isEliminated()) {
      throw new IllegalStateException("FourHamsters: Target player is already eliminated.");
    }

    List<Card> matchingCards = requester.getHand().stream()
        .filter(c -> hamsterType.equalsIgnoreCase(c.getType()))
        .limit(REQUIRED_COUNT)
        .toList();

    if (matchingCards.size() < REQUIRED_COUNT) {
      throw new IllegalStateException(String.format(
        "FourHamsters: requester does not hold %d cards of type %s (found %d)",
        REQUIRED_COUNT, hamsterType, matchingCards.size()));
    }

    matchingCards.forEach(c -> requester.removeFromHand(c.getId()));

    target.decrementLives();
    requester.incrementLives();

    String message = String.format(
        "%s played four %s cards and stole 1 life from %s!",
        requester.getName(), hamsterType, target.getName());

    if (target.isEliminated()) {
      message += " " + target.getName() + " was eliminated!";
    }

    return CardCommandResult.publicOnly(message);
  }
}

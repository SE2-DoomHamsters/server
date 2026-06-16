package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import com.doomhamsters.gamesession.cardcommands.CardDefinition;
import org.springframework.stereotype.Component;

/**
 * Card definition for playing 4 identical Hamsters to steal 1 life.
 */
@Component
public class FourHamstersCardDefinition implements CardDefinition {

  private static final int REQUIRED_COUNT = 4;

  public FourHamstersCardDefinition() {
  }

  @Override
  public String cardType() {
    return "HAMSTER_FOUR";
  }

  @Override
  public String commandId() {
    return "FOUR_HAMSTERS";
  }

  @Override
  public String displayName() {
    return "Hamster Combo: 4-of-a-Kind";
  }

  @Override
  public Card createTestingCard(String playerId) {
    return new Card("four_hamsters_" + playerId, displayName(), cardType());
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    String targetPlayerId = requiredString(context, "targetPlayerId");
    String hamsterType = requiredString(context, "hamsterType");

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

    // 1. Die 4 Karten suchen und abwerfen
    java.util.List<Card> matchingCards = requester.getHand().stream()
        .filter(c -> hamsterType.equalsIgnoreCase(c.getType()))
        .limit(REQUIRED_COUNT)
        .toList();

    if (matchingCards.size() < REQUIRED_COUNT) {
      throw new IllegalStateException(String.format(
        "FourHamsters: requester does not hold %d cards of type %s (found %d)",
        REQUIRED_COUNT, hamsterType, matchingCards.size()));
    }

    // 2. Jetzt gefahrlos abwerfen, da wir über unsere temporäre Liste iterieren
    matchingCards.forEach(c -> requester.removeFromHand(c.getId()));

    // 2. Leben klauen
    target.decrementLives();
    requester.incrementLives();

    // 3. Nachricht generieren
    String message = String.format(
        "%s played four %s cards and stole 1 life from %s!",
        requester.getName(), hamsterType, target.getName());

    if (target.isEliminated()) {
      message += " " + target.getName() + " was eliminated!";
    }

    return CardCommandResult.publicOnly(message);
  }

  // Hilfsmethode zum sicheren Auslesen der Parameter
  private static String requiredString(CardCommandContext context, String key) {
    Object value = context.getParameters().get(key);
    if (value == null || value.toString().isBlank()) {
      throw new IllegalArgumentException("FourHamsters: missing required parameter: " + key);
    }
    return value.toString();
  }
}

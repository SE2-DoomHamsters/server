package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import com.doomhamsters.gamesession.cardcommands.CardDefinition;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

/**
 * Card definition for playing 2 identical Hamsters to steal a random card.
 */
@Component
public class TwoHamstersCardDefinition implements CardDefinition {

  private static final int REQUIRED_COUNT = 2;

  public TwoHamstersCardDefinition() {
  }

  @Override
  public String cardType() {
    return "hamster_two";
  }

  @Override
  public String commandId() {
    return "TWO_HAMSTERS";
  }

  @Override
  public String displayName() {
    return "Hamster Combo: 2-of-a-Kind";
  }

  @Override
  public Card createTestingCard(String playerId) {
    return new Card("two_hamsters_" + playerId, displayName(), cardType());
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    String targetPlayerId = requiredString(context, "targetPlayerId");
    String hamsterType = requiredString(context, "hamsterType");

    if (!hamsterType.startsWith("hamster_")) {
      throw new IllegalArgumentException(
        "TwoHamsters: hamsterType must start with 'hamster_', got: " + hamsterType);
    }

    Player requester = context.getPlayer();
    Player target = context.getGame().getPlayers().stream()
      .filter(p -> p.getId().equals(targetPlayerId))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException(
        "TwoHamsters: unknown targetPlayerId=" + targetPlayerId));

    if (target.getId().equals(requester.getId())) {
      throw new IllegalArgumentException("TwoHamsters: cannot target yourself");
    }

    if (target.isEliminated()) {
      throw new IllegalStateException("TwoHamsters: Target player is already eliminated.");
    }

    // 1. Die 2 Karten suchen
    List<Card> matchingCards = requester.getHand().stream()
      .filter(c -> hamsterType.equalsIgnoreCase(c.getType()))
      .limit(REQUIRED_COUNT)
      .toList();

    if (matchingCards.size() < REQUIRED_COUNT) {
      throw new IllegalStateException(String.format(
        "TwoHamsters: requester does not hold %d cards of type %s",
        REQUIRED_COUNT, hamsterType));
    }

    // 2. Die 2 Karten gefahrlos abwerfen
    matchingCards.forEach(c -> requester.removeFromHand(c.getId()));

    // 3. Steal-Logik (orientiert an StealCardDefinition)
    if (target.getHand().isEmpty()) {
      return CardCommandResult.publicOnly(
        requester.getName() + " played two " + hamsterType + " cards, but "
          + target.getName() + " had no cards to steal!");
    }

    int randomIndex = ThreadLocalRandom.current().nextInt(target.getHand().size());
    Card cardToSteal = target.getHand().get(randomIndex);
    Card stolenCard = target.removeFromHand(cardToSteal.getId());
    requester.addToHand(stolenCard);

    // 4. Resultat mit privateResult generieren
    String publicMsg = String.format(
      "%s played two %s cards and stole a random card from %s!",
      requester.getName(), hamsterType, target.getName());

    String privateMsg = "You successfully stole: " + stolenCard.getName();

    return CardCommandResult.withPrivateResult(publicMsg, privateMsg, stolenCard);
  }

  // Hilfsmethode zum sicheren Auslesen der Parameter
  private static String requiredString(CardCommandContext context, String key) {
    Object value = context.getParameters().get(key);
    if (value == null || value.toString().isBlank()) {
      throw new IllegalArgumentException("TwoHamsters: missing required parameter: " + key);
    }
    return value.toString();
  }
}

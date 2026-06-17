package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import com.doomhamsters.gamesession.cardcommands.CardDefinition;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Card definition for the Hamster Trio combo.
 *
 * <p>Activated when a player plays three identical Hamster cards. The acting player names a target
 * and a specific card type. If the target holds at least one card of that type, one copy is moved
 * to the acting player's hand. If the target has no matching card, nothing is transferred.
 *
 * <p>The three Hamster cards are discarded before the effect resolves, regardless of outcome.
 *
 * <p>Required parameters:
 *
 * <ul>
 *   <li>{@code targetPlayerId} – the id of the player being targeted
 *   <li>{@code cardType} – the card type to take
 *   <li>{@code hamsterType} – the hamster variant (e.g. {@code "hamster_ninja"}); must start with
 *       {@code "hamster_"}
 * </ul>
 */
@Component
public class HamsterTrioCardDefinition implements CardDefinition {

  private static final int REQUIRED_COUNT = 3;

  /** Creates the Hamster Trio card definition. */
  public HamsterTrioCardDefinition() {}

  @Override
  public String cardType() {
    return "HamsterTrio";
  }

  @Override
  public String commandId() {
    return "HAMSTER_TRIO";
  }

  @Override
  public String displayName() {
    return "Hamster Trio";
  }

  @Override
  public Card createTestingCard(String playerId) {
    return new Card("hamster_trio_" + playerId, displayName(), cardType());
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    String targetPlayerId = requiredString(context, "targetPlayerId");
    final String requestedType  = requiredString(context, "cardType");
    String hamsterType    = requiredString(context, "hamsterType");

    if (!hamsterType.startsWith("hamster_")) {
      throw new IllegalArgumentException(
        "HamsterTrio: hamsterType must start with 'hamster_', got: " + hamsterType);
    }

    Player requester = context.getPlayer();
    Player target = context.getGame().getPlayers().stream()
        .filter(p -> p.getId().equals(targetPlayerId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
        "HamsterTrio: unknown targetPlayerId=" + targetPlayerId));

    if (target.getId().equals(requester.getId())) {
      throw new IllegalArgumentException("HamsterTrio: cannot target yourself");
    }

    if (target.isEliminated()) {
      throw new IllegalStateException("HamsterTrio: target player is already eliminated");
    }

    // Collect the three matching cards first, then discard them
    List<Card> matchingCards = requester.getHand().stream()
        .filter(c -> hamsterType.equalsIgnoreCase(c.getType()))
        .limit(REQUIRED_COUNT)
        .toList();

    if (matchingCards.size() < REQUIRED_COUNT) {
      throw new IllegalStateException(String.format(
        "HamsterTrio: requester does not hold %d cards of type %s",
        REQUIRED_COUNT, hamsterType));
    }

    matchingCards.forEach(c -> requester.removeFromHand(c.getId()));

    Card found = target.getHand().stream()
        .filter(c -> requestedType.equalsIgnoreCase(c.getType()))
        .findFirst()
        .orElse(null);

    if (found == null) {
      return CardCommandResult.publicOnly(String.format(
        "%s played three %s cards and demanded a %s from %s — but they had none.",
        requester.getName(), hamsterType, requestedType, target.getName()));
    }

    target.removeFromHand(found.getId());
    requester.addToHand(found);

    return CardCommandResult.publicOnly(String.format(
      "%s played three %s cards and took a %s from %s.",
      requester.getName(), hamsterType, requestedType, target.getName()));
  }

  private static String requiredString(CardCommandContext context, String key) {
    Object value = context.getParameters().get(key);
    if (value == null || value.toString().isBlank()) {
      throw new IllegalArgumentException("HamsterTrio: missing required parameter: " + key);
    }
    return value.toString();
  }
}

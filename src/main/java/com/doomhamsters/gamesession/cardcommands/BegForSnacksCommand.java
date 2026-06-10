package com.doomhamsters.gamesession.cardcommands;

import com.doomhamsters.Card;
import com.doomhamsters.Player;

/**
 * Command that transfers a card of a requested type from a target player to the acting player.
 *
 * <p>The acting player names a target and a card type. If the target holds at least one card of
 * that type, one copy is moved to the acting player's hand. If the target has no matching card,
 * nothing is transferred and the turn continues unchanged.
 *
 * <p>The {@code Beg for Snacks} card itself is always discarded from the acting player's hand
 * before the effect resolves, regardless of outcome.
 *
 * <p>Required parameters:
 *
 * <ul>
 *   <li>{@code targetPlayerId} – the id of the player being begged
 *   <li>{@code cardType} – the {@link com.doomhamsters.Card#getType() type} of the card to request
 * </ul>
 */
public class BegForSnacksCommand implements CardCommand {

  public static final BegForSnacksCommand INSTANCE = new BegForSnacksCommand();

  private BegForSnacksCommand() {}

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    String targetPlayerId = requiredString(context, "targetPlayerId");
    String requestedType  = requiredString(context, "cardType");

    Player requester = context.getPlayer();
    Player target = context.getGame().getPlayers().stream()
        .filter(p -> p.getId().equals(targetPlayerId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
        "BegForSnacks: unknown targetPlayerId=" + targetPlayerId));

    if (target.getId().equals(requester.getId())) {
      throw new IllegalArgumentException("BegForSnacks: cannot target yourself");
    }

    // Remove the played card from hand before resolving the effect
    requester.removeFromHand(context.getCard().getId());

    Card found = target.getHand().stream()
        .filter(c -> requestedType.equalsIgnoreCase(c.getType()))
        .findFirst()
        .orElse(null);

    if (found == null) {
      return CardCommandResult.publicOnly(String.format(
        "%s begged %s for a %s — but they had none.",
        requester.getName(), target.getName(), requestedType));
    }

    target.removeFromHand(found.getId());
    requester.addToHand(found);

    return CardCommandResult.publicOnly(String.format(
      "%s begged %s for a %s and received one.",
      requester.getName(), target.getName(), requestedType));
  }

  private static String requiredString(CardCommandContext context, String key) {
    Object value = context.getParameters().get(key);
    if (value == null || value.toString().isBlank()) {
      throw new IllegalArgumentException("BegForSnacks: missing required parameter: " + key);
    }
    return value.toString();
  }
}


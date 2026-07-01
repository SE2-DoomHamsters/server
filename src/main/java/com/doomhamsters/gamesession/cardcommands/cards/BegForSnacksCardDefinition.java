package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.AbstractCardDefinition;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import org.springframework.stereotype.Component;

/**
 * Card definition for Beg for Snacks.
 *
 * <p>The acting player names a target and a card type. If the target holds at least one card of
 * that type, one copy is moved to the acting player's hand. If the target has no matching card,
 * nothing is transferred and the turn continues unchanged.
 *
 * <p>Required parameters:
 *
 * <ul>
 *   <li>{@code targetPlayerId} - the id of the player being begged
 *   <li>{@code cardType} - the card type to request
 * </ul>
 */
@Component
public class BegForSnacksCardDefinition extends AbstractCardDefinition {

  public BegForSnacksCardDefinition() {
    super("BegForSnacks", "BEG_FOR_SNACKS", "Beg for Snacks");
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    String targetPlayerId = requiredParam(context, "targetPlayerId");
    String requestedType = requiredParam(context, "cardType");

    Player requester = context.getPlayer();
    Player target = context.getGame().getPlayers().stream()
        .filter(player -> player.getId().equals(targetPlayerId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "BegForSnacks: unknown targetPlayerId=" + targetPlayerId));

    if (target.getId().equals(requester.getId())) {
      throw new IllegalArgumentException("BegForSnacks: cannot target yourself");
    }

    Card found = target.getHand().stream()
        .filter(card -> requestedType.equalsIgnoreCase(card.getType()))
        .findFirst()
        .orElse(null);

    if (found == null) {
      return CardCommandResult.publicOnly(String.format(
          "%s begged %s for a %s - but they had none.",
          requester.getName(), target.getName(), requestedType));
    }

    target.removeFromHand(found.getId());
    requester.addToHand(found);

    return CardCommandResult.publicOnly(String.format(
        "%s begged %s for a %s and received one.",
        requester.getName(), target.getName(), requestedType));
  }
}

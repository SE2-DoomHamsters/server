package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.gamesession.cardcommands.AbstractCardDefinition;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import org.springframework.stereotype.Component;

/**
 * Card definition for Tunnel Chaos.
 *
 * <p>Activating the card shuffles the draw deck and announces the effect to all players. The card
 * has no target and produces no private information.
 */
@Component
public class TunnelChaosCardDefinition extends AbstractCardDefinition {

  public TunnelChaosCardDefinition() {
    super("TunnelChaos", "TUNNEL_CHAOS", "Tunnel Chaos");
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    context.getGame().getDeck().shuffle();

    return CardCommandResult.publicOnly(
        context.getPlayer().getName() + " activated Tunnel Chaos and shuffled the deck.");
  }
}

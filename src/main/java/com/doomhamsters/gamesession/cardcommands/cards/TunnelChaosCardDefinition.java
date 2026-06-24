package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import com.doomhamsters.gamesession.cardcommands.CardDefinition;
import org.springframework.stereotype.Component;

/**
 * Card definition for Tunnel Chaos.
 *
 * <p>Activating the card shuffles the draw deck and announces the effect to all players. The card
 * has no target and produces no private information.
 */
@Component
public class TunnelChaosCardDefinition implements CardDefinition {

  /** Creates the Tunnel Chaos card definition. */
  public TunnelChaosCardDefinition() {}

  @Override
  public String cardType() {
    return "TunnelChaos";
  }

  @Override
  public String commandId() {
    return "TUNNEL_CHAOS";
  }

  @Override
  public String displayName() {
    return "Tunnel Chaos";
  }

  @Override
  public Card createTestingCard(String playerId) {
    return new Card("tunnel_chaos_" + playerId, displayName(), cardType());
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    context.getGame().getDeck().shuffle();

    return CardCommandResult.publicOnly(
        context.getPlayer().getName() + " activated Tunnel Chaos and shuffled the deck.");
  }
}

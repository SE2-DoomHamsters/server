package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.gamesession.cardcommands.AbstractCardDefinition;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import org.springframework.stereotype.Component;

/**
 * Card definition for Hyper Mode.
 */
@Component
public class HyperModeCardDefinition extends AbstractCardDefinition {

  public HyperModeCardDefinition() {
    super("HyperMode", "HYPER_MODE", "Hyper Mode");
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    context.getGame()
        .getBoard()
        .addExtraTurn();

    return CardCommandResult.publicOnly(
        context.getPlayer().getName()
            + " activated Hyper Mode. "
            + "The next player gains an extra turn.");
  }
}

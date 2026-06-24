package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.gamesession.cardcommands.AbstractCardDefinition;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import org.springframework.stereotype.Component;

/**
 * Card definition for Power Nap.
 */
@Component
public class PowerNapCardDefinition extends AbstractCardDefinition {

  public PowerNapCardDefinition() {
    super("PowerNap", "POWER_NAP", "Power Nap");
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    context.getGame().advanceTurn();

    return CardCommandResult.publicOnly(
        context.getPlayer().getName()
            + " activated Power Nap and skipped drawing.");
  }
}

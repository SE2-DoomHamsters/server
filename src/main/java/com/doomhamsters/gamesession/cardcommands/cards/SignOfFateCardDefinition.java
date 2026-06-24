package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.gamesession.cardcommands.AbstractCardDefinition;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import org.springframework.stereotype.Component;

/**
 * Card definition for Sign of Fate.
 */
@Component
public class SignOfFateCardDefinition extends AbstractCardDefinition {

  public SignOfFateCardDefinition() {
    super("SignOfFate", "SIGN_OF_FATE", "Sign of Fate");
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    context.getPlayer().addLives(1);

    return CardCommandResult.publicOnly(
        context.getPlayer().getName()
            + " activated Sign of Fate and gained +1 life.");
  }
}

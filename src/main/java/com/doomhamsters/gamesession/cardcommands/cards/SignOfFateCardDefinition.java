package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import com.doomhamsters.gamesession.cardcommands.CardDefinition;
import org.springframework.stereotype.Component;

/**
 * Card definition for Sign of Fate.
 */
@Component
public class SignOfFateCardDefinition implements CardDefinition {


  @Override
  public String cardType() {
    return "SignOfFate";
  }

  @Override
  public String commandId() {
    return "SIGN_OF_FATE";
  }

  @Override
  public String displayName() {
    return "Sign of Fate";
  }

  @Override
  public Card createTestingCard(String playerId) {
    return new Card("sign_of_fate_" + playerId, displayName(), cardType());
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    context.getPlayer().addLives(1);

    return CardCommandResult.publicOnly(
        context.getPlayer().getName()
            + " activated Sign of Fate and gained +1 life.");
  }
}

package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import com.doomhamsters.gamesession.cardcommands.CardDefinition;
import org.springframework.stereotype.Component;

/**
 * Card definition for Power Nap.
 */
@Component
public class PowerNapCardDefinition implements CardDefinition {

  /**
   * Creates the Power Nap card definition.
   */
  public PowerNapCardDefinition() {
    // Required by Spring component scanning for this stateless card definition.
  }

  @Override
  public String cardType() {
    return "PowerNap";
  }

  @Override
  public String commandId() {
    return "POWER_NAP";
  }

  @Override
  public String displayName() {
    return "Power Nap";
  }

  @Override
  public Card createTestingCard(String playerId) {
    return new Card(
        "power_nap_" + playerId,
        displayName(),
        cardType());
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    context.getGame().advanceTurn();

    return CardCommandResult.publicOnly(
        context.getPlayer().getName()
            + " activated Power Nap and skipped drawing.");
  }
}

package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import com.doomhamsters.gamesession.cardcommands.CardDefinition;
import org.springframework.stereotype.Component;

/**
 * Card definition for Hyper Mode.
 */
@Component
public class HyperModeCardDefinition implements CardDefinition {

  /**
   * Creates the Hyper Mode card definition.
   */
  public HyperModeCardDefinition() {
  }

  @Override
  public String cardType() {
    return "HyperMode";
  }

  @Override
  public String commandId() {
    return "HYPER_MODE";
  }

  @Override
  public String displayName() {
    return "Hyper Mode";
  }

  @Override
  public Card createTestingCard(String playerId) {
    return new Card(
        "hyper_mode_" + playerId,
        displayName(),
        cardType());
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
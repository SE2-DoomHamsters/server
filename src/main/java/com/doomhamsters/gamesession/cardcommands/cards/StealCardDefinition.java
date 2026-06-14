package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import com.doomhamsters.gamesession.cardcommands.CardDefinition;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Component;



/**
 * Card definition for Steal Card.
 */
@Component
public class StealCardDefinition implements CardDefinition {
  public StealCardDefinition() {

  }

  @Override
  public String cardType() {
    return "StealCard";
  }

  @Override
  public String commandId() {
    return "STEAL_CARD";
  }

  @Override
  public String displayName() {
    return "Steal Card";
  }

  @Override
  public Card createTestingCard(String playerId) {
    return new Card("steal_card_" + playerId, displayName(), cardType());
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    // Parametern auslesen (die vom Frontend kommen)
    Map<String, Object> parameters = context.getParameters();
    String targetPlayerId = (String) parameters.get("targetPlayerId");

    if (targetPlayerId == null) {
      throw new IllegalArgumentException("Target player ID is missing.");
    }

    // Spieler-Objekte holen
    Player thief = context.getPlayer();
    Player victim = context.getGame().getPlayers().stream()
      .filter(p -> p.getId().equals(targetPlayerId))
      .findFirst()
      .orElse(null);

    if (victim == null || victim.getHand().isEmpty()) {
      return CardCommandResult.publicOnly(
        thief.getName() + " tried to steal, but " +
          (victim != null ? victim.getName() : "the target") + " had no cards!"
      );
    }

    // Zufällige Karte klauen
    int randomIndex = new Random().nextInt(victim.getHand().size());
    Card cardToSteal = victim.getHand().get(randomIndex);
    Card stolenCard = victim.removeFromHand(cardToSteal.getId());
    thief.addToHand(stolenCard);

    // Ergebnis zurückgeben
    return CardCommandResult.withPrivateResult(
      thief.getName() + " stole a card from " + victim.getName() + "!",
      "You successfully stole: " + stolenCard.getName(),
      stolenCard
    );
  }
}

package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import com.doomhamsters.gamesession.cardcommands.CardDefinition;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Card definition for playing 4 identical Hamsters to steal a life.
 */
@Component
public class FourHamstersCardDefinition implements CardDefinition {

  /**
   * Creates the FourHamsters card definition.
   */
  public FourHamstersCardDefinition() {
    // Required by Spring component scanning
  }

  @Override
  public String cardType() {
    return "Normal";
  }

  @Override
  public String commandId() {
    return "FOUR_HAMSTERS";
  }

  @Override
  public String displayName() {
    return "Hamster Combo: 4-of-a-Kind — Steal 1 Life";
  }

  @Override
  public Card createTestingCard(String playerId) {
    return new Card(
      "four_hamsters_" + playerId,
      displayName(),
      cardType());
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    Map<String, Object> params = context.getParameters();
    Player initiator = context.getPlayer();

    // 1. Zielspieler aus den Parametern auslesen
    String targetPlayerId = (String) params.get("targetPlayerId");
    if (targetPlayerId == null) {
      throw new IllegalArgumentException("Target player ID is required to steal a life.");
    }

    // 2. Zielspieler im aktuellen Spiel finden
    Player target = context.getGame().getPlayers().stream()
      .filter(p -> p.getId().equals(targetPlayerId))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("Target player not found."));

    if (target.isEliminated()) {
      throw new IllegalStateException("Target player is already eliminated.");
    }

    // 3. Leben verschieben via fachliche Domänen-Methoden
    target.decrementLives();
    initiator.incrementLives();

    // 4. Öffentliche Nachricht generieren
    String message = initiator.getName() + " played 4 identical cards and stole 1 life from " + target.getName() + "!";
    if (target.isEliminated()) {
      message += " " + target.getName() + " was eliminated!";
    }

    // Rückmeldung an alle WebSocket-Clients senden
    return CardCommandResult.publicOnly(message);
  }
}

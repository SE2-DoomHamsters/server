package com.doomhamsters.gamesession.cardcommands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CardCommandContextTest {

  @Test
  void storesReferencesAndCopiesMutableInputs() {
    GameSession session = new GameSession("game-1", "lobby-1");
    Game game = session.getGame();
    Player player = new Player("p1", "Alice");
    Card card = new Card("card-1", "Quick Peek", "action");
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("target", "deck");

    CardCommandContext context =
        new CardCommandContext(session, game, player, card, parameters);
    parameters.put("target", "hand");

    assertSame(session, context.getSession());
    assertSame(game, context.getGame());
    assertSame(player, context.getPlayer());
    assertEquals("deck", context.getParameters().get("target"));
    Map<String, Object> copiedParameters = context.getParameters();
    assertThrows(UnsupportedOperationException.class,
        () -> copiedParameters.put("other", "value"));

    Card returnedCard = context.getCard();
    assertEquals("card-1", returnedCard.getId());
    assertEquals("Quick Peek", returnedCard.getName());
    assertNotSame(returnedCard, context.getCard());
  }

  @Test
  void usesEmptyParametersWhenNullIsProvided() {
    GameSession session = new GameSession("game-1", "lobby-1");

    CardCommandContext context = new CardCommandContext(
        session,
        session.getGame(),
        new Player("p1", "Alice"),
        new Card("card-1", "Quick Peek", "action"),
        null);

    assertTrue(context.getParameters().isEmpty());
  }
}

package com.doomhamsters.gamesession.cardcommands.cards;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.doomhamsters.Card;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BegForSnacksCardDefinitionTest {

  private static final BegForSnacksCardDefinition DEFINITION = new BegForSnacksCardDefinition();

  private GameSession session;
  private Player requester;
  private Card begCard;

  @BeforeEach
  void setUp() {
    session = new GameSession("game-1", "lobby-1");
    requester = new Player("p1", "Alice");
    begCard   = new Card("bfs_p1", DEFINITION.displayName(), DEFINITION.cardType());

    requester.addToHand(begCard);
  }

  private CardCommandContext ctx(Map<String, Object> params) {
    return new CardCommandContext(session, session.getGame(), requester, begCard, params);
  }

  // ── metadata ─────────────────────────────────────────────────────

  @Test
  void cardType_matchesExpected() {
    assertEquals("BegForSnacks", DEFINITION.cardType());
  }

  @Test
  void commandId_matchesExpected() {
    assertEquals("BegForSnacks", DEFINITION.commandId());
  }

  @Test
  void effectId_defaultsToCommandId() {
    assertEquals(DEFINITION.commandId(), DEFINITION.effectId());
  }

  @Test
  void createTestingCard_containsPlayerId() {
    Card card = DEFINITION.createTestingCard("p1");
    assertTrue(card.getId().contains("p1"));
    assertEquals(DEFINITION.cardType(), card.getType());
  }

  // ── validation ───────────────────────────────────────────────────

  @Test
  void missingTargetPlayerId_throws() {
    CardCommandContext context = ctx(Map.of("cardType", "power_nap"));
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
      () -> DEFINITION.execute(context));
    assertTrue(ex.getMessage().contains("targetPlayerId"));
  }

  @Test
  void missingCardType_throws() {
    CardCommandContext context = ctx(Map.of("targetPlayerId", "p2"));
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
      () -> DEFINITION.execute(context));
    assertTrue(ex.getMessage().contains("cardType"));
  }

  @Test
  void unknownTargetPlayerId_throws() {
    CardCommandContext context = ctx(Map.of("targetPlayerId", "nobody", "cardType", "power_nap"));
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
      () -> DEFINITION.execute(context));
    assertTrue(ex.getMessage().contains("unknown targetPlayerId"));
  }

  @Test
  void blankTargetPlayerId_throws() {
    CardCommandContext context = ctx(Map.of("targetPlayerId", " ", "cardType", "power_nap"));
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
      () -> DEFINITION.execute(context));
    assertTrue(ex.getMessage().contains("targetPlayerId"));
  }

  @Test
  void blankCardType_throws() {
    CardCommandContext context = ctx(Map.of("targetPlayerId", "p2", "cardType", " "));
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
      () -> DEFINITION.execute(context));
    assertTrue(ex.getMessage().contains("cardType"));
  }
}

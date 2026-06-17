package com.doomhamsters.gamesession.cardcommands.cards;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.doomhamsters.Card;
import com.doomhamsters.Deck;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TunnelChaosCardDefinitionTest {

  private static final TunnelChaosCardDefinition DEFINITION = new TunnelChaosCardDefinition();

  private GameSession session;
  private Game game;
  private Player player;
  private Card tunnelCard;

  @BeforeEach
  void setUp() {
    session = new GameSession("game-1", "lobby-1");
    game = session.getGame();
    player = new Player("p1", "Alice");
    tunnelCard = new Card("tunnel_chaos_p1", DEFINITION.displayName(), DEFINITION.cardType());

    List<Card> deckCards = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      deckCards.add(new Card("normal_" + i, "Normal", "normal"));
    }
    game.setDeck(new Deck(deckCards));
  }

  private CardCommandContext ctx() {
    return new CardCommandContext(session, game, player, tunnelCard, Map.of());
  }

  // ── metadata ─────────────────────────────────────────────────────

  @Test
  void cardType_matchesExpected() {
    assertEquals("TunnelChaos", DEFINITION.cardType());
  }

  @Test
  void commandId_matchesExpected() {
    assertEquals("TUNNEL_CHAOS", DEFINITION.commandId());
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

  // ── effect ───────────────────────────────────────────────────────

  @Test
  void execute_announcesEffectPublicly() {
    CardCommandResult result = DEFINITION.execute(ctx());
    assertTrue(result.getPublicMessage().contains("Alice"));
    assertTrue(result.getPublicMessage().contains("Tunnel Chaos"));
  }

  @Test
  void execute_hasNoPrivateResult() {
    CardCommandResult result = DEFINITION.execute(ctx());
    assertFalse(result.hasPrivateResult());
  }

  @Test
  void execute_preservesDeckSize() {
    int sizeBefore = game.getDeck().size();
    DEFINITION.execute(ctx());
    assertEquals(sizeBefore, game.getDeck().size());
  }

  @Test
  void execute_keepsSameCards() {
    List<String> idsBefore = deckCardIds();
    DEFINITION.execute(ctx());
    assertEquals(idsBefore, deckCardIds());
  }

  private List<String> deckCardIds() {
    List<String> ids = new ArrayList<>(
        game.getDeck().getCards().stream().map(Card::getId).toList());
    Collections.sort(ids);
    return ids;
  }
}

package com.doomhamsters.gamesession.cardcommands.cards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.doomhamsters.Board;
import com.doomhamsters.Card;
import com.doomhamsters.Deck;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SquickCardDefinitionTest {

  private static final SquickCardDefinition DEFINITION = new SquickCardDefinition();

  private GameSession session;
  private Game game;
  private Player alice;
  private Player bob;

  @BeforeEach
  void setUp() {
    session = new GameSession("game-1", "lobby-1");
    game = session.getGame();

    game.setPlayers(List.of(new Player("p1", "Alice"), new Player("p2", "Bob")));
    game.setDeck(new Deck());
    game.setBoard(new Board(new ArrayList<>(game.getPlayers()), new Deck()));

    alice = gamePlayer("p1");
    bob = gamePlayer("p2");
  }

  // ── metadata ─────────────────────────────────────────────────────────────

  @Test
  void cardType_matchesExpected() {
    assertEquals("Squick", DEFINITION.cardType());
  }

  @Test
  void commandId_matchesExpected() {
    assertEquals("SQUICK", DEFINITION.commandId());
  }

  @Test
  void displayName_matchesExpected() {
    assertEquals("Squick", DEFINITION.displayName());
  }

  @Test
  void isUndoable_returnsFalse() {
    assertFalse(DEFINITION.isUndoable());
  }

  @Test
  void createTestingCard_containsPlayerId() {
    Card card = DEFINITION.createTestingCard("p1");
    assertTrue(card.getId().contains("p1"));
    assertEquals(DEFINITION.cardType(), card.getType());
  }

  // ── happy path ───────────────────────────────────────────────────────────

  @Test
  void execute_restoresGameStateToBeforeLastCommand() {
    // Bob gets a card that will be "stolen" by an effect
    Card target = new Card("c1", "QuickPeek", "QuickPeek");
    bob.addToHand(target);

    // Snapshot the state before the effect runs (as the controller would)
    game.getBoard().recordLastAction(new Game(game), "STEAL_CARD");

    // Simulate the effect: card moved from Bob to Alice
    bob.removeFromHand("c1");
    alice.addToHand(target);
    assertTrue(bob.getHand().isEmpty());
    assertEquals(1, alice.getHand().size());

    CardCommandResult result = DEFINITION.execute(ctx(alice));

    assertTrue(result.getPublicMessage().contains("Alice"));
    assertTrue(result.getPublicMessage().contains("cancelled the last card effect"));
    assertFalse(result.hasPrivateResult());

    // Restored game: Bob has his card back
    Player restoredBob = restoredPlayer("p2");
    assertEquals(1, restoredBob.getHand().size());
    assertEquals("c1", restoredBob.getHand().get(0).getId());

    // Restored game: Alice does not have the stolen card
    Player restoredAlice = restoredPlayer("p1");
    assertTrue(restoredAlice.getHand().stream().noneMatch(c -> c.getId().equals("c1")));
  }

  @Test
  void execute_removesSquickFromRestoredHand_preventingInfiniteUndo() {
    // Give Alice a Squick card and record it as existing before the effect
    Card squick = new Card("sq_p1", DEFINITION.displayName(), DEFINITION.cardType());
    alice.addToHand(squick);

    // Snapshot (Squick is in Alice's hand at this point)
    game.getBoard().recordLastAction(new Game(game), "TUNNEL_CHAOS");

    // Simulate the effect (just some mutation)
    game.getDeck().shuffle();

    // Play Squick — use the same card id as the one in Alice's hand
    CardCommandContext ctx = new CardCommandContext(
        session, game, alice, squick, Map.of());
    DEFINITION.execute(ctx);

    // In the restored state, Alice should NOT still have the Squick card
    Player restoredAlice = restoredPlayer("p1");
    assertTrue(restoredAlice.getHand().stream().noneMatch(c -> c.getId().equals("sq_p1")));
  }

  @Test
  void execute_snapshotIsConsumed_secondSquickThrows() {
    game.getBoard().recordLastAction(new Game(game), "POWER_NAP");
    DEFINITION.execute(ctx(alice));

    // Snapshot was consumed; a second Squick on the restored game must fail
    Game restoredGame = session.getGame();
    CardCommandContext ctx2 = new CardCommandContext(
        session, restoredGame, restoredPlayer("p1"),
        new Card("sq2", DEFINITION.displayName(), DEFINITION.cardType()),
        Map.of());

    assertThrows(IllegalStateException.class, () -> DEFINITION.execute(ctx2));
  }

  // ── guard conditions ─────────────────────────────────────────────────────

  @Test
  void execute_throwsWhenNoSnapshotExists() {
    // No recordLastAction call — snapshot is null
    CardCommandContext ctxNoSnapshot = ctx(alice);
    assertThrows(IllegalStateException.class, () -> DEFINITION.execute(ctxNoSnapshot));
  }

  @Test
  void execute_throwsWhenLastCommandWasAlsoSquick() {
    game.getBoard().recordLastAction(new Game(game), "SQUICK");
    CardCommandContext ctxSquick = ctx(alice);
    assertThrows(IllegalStateException.class, () -> DEFINITION.execute(ctxSquick));
  }

  // ── helpers ──────────────────────────────────────────────────────────────

  private Player gamePlayer(String id) {
    return game.getPlayers().stream()
        .filter(p -> p.getId().equals(id))
        .findFirst()
        .orElseThrow();
  }

  private Player restoredPlayer(String id) {
    return session.getGame().getPlayers().stream()
        .filter(p -> p.getId().equals(id))
        .findFirst()
        .orElseThrow();
  }

  private CardCommandContext ctx(Player player) {
    Card squickCard = new Card(
        "sq_" + player.getId(), DEFINITION.displayName(), DEFINITION.cardType());
    return new CardCommandContext(session, game, player, squickCard, Map.of());
  }
}

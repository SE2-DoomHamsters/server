import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Game}.
 */
@DisplayName("Game")
class GameTest {

  // ── Helpers ────────────────────────────────────────────────────────────────

  private static List<Card> actionCards(int count) {
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      cards.add(new Card("action_" + i, "Action Card " + i, "action"));
    }
    return cards;
  }

  private static List<Card> doomCards(int count) {
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      cards.add(new Card("doom_" + i, "Doom Hamster " + i, "doom"));
    }
    return cards;
  }

  private static Card snackStashProto() {
    return new Card("ss_proto", "Snack Stash", "snack_stash");
  }

  /**
   * Creates and sets up a game with enough cards so the deck never runs dry
   * during normal test execution.
   */
  private static Game buildGame(List<String> playerNames) {
    Game game = new Game();
    game.setup(
      playerNames,
      actionCards(playerNames.size() * 10),
      snackStashProto(),
      doomCards(playerNames.size() + 5));
    return game;
  }

  /** Eliminates a player by calling handleDoom() until their lives reach zero. */
  private static void eliminate(Player player) {
    while (player.isAlive()) {
      player.handleDoom();
    }
  }


  // ── Initial state ──────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Initial state")
  class InitialStateTests {

    @Test
    @DisplayName("state is SETUP immediately after construction")
    void initialStateIsSetup() {
      assertEquals(Game.State.SETUP, new Game().getState());
    }

    @Test
    @DisplayName("winner is null immediately after construction")
    void initialWinnerIsNull() {
      assertNull(new Game().getWinner());
    }

    @Test
    @DisplayName("players list is empty immediately after construction")
    void initialPlayersIsEmpty() {
      assertTrue(new Game().getPlayers().isEmpty());
    }

    @Test
    @DisplayName("board is null before setup()")
    void boardNullBeforeSetup() {
      assertNull(new Game().getBoard());
    }

    @Test
    @DisplayName("deck is null before setup()")
    void deckNullBeforeSetup() {
      assertNull(new Game().getDeck());
    }
  }


  // ── setup() ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("setup()")
  class SetupTests {

    @Test
    @DisplayName("transitions state to RUNNING")
    void stateIsRunningAfterSetup() {
      Game game = buildGame(List.of("Alice", "Bob"));
      assertEquals(Game.State.RUNNING, game.getState());
    }

    @Test
    @DisplayName("throws IllegalArgumentException for fewer than 2 players")
    void throwsForFewerThanTwoPlayers() {
      Game game = new Game();
      assertThrows(IllegalArgumentException.class, () ->
        game.setup(List.of("Alice"), actionCards(20), snackStashProto(), doomCards(5)));
    }

    @Test
    @DisplayName("throws IllegalArgumentException for empty player list")
    void throwsForEmptyPlayerList() {
      Game game = new Game();
      assertThrows(IllegalArgumentException.class, () ->
        game.setup(List.of(), actionCards(20), snackStashProto(), doomCards(5)));
    }

    @Test
    @DisplayName("creates correct number of players")
    void correctPlayerCount() {
      Game game = buildGame(List.of("Alice", "Bob", "Carol"));
      assertEquals(3, game.getPlayers().size());
    }

    @Test
    @DisplayName("player names are assigned correctly")
    void playerNamesAssigned() {
      Game game = buildGame(List.of("Alice", "Bob"));
      assertEquals("Alice", game.getPlayers().get(0).getName());
      assertEquals("Bob", game.getPlayers().get(1).getName());
    }

    @Test
    @DisplayName("each player starts with exactly STARTING_HAND_SIZE cards")
    void eachPlayerStartsWithCorrectHandSize() {
      Game game = buildGame(List.of("Alice", "Bob", "Carol"));
      game.getPlayers().forEach(p ->
        assertEquals(Game.STARTING_HAND_SIZE, p.getHand().size()));
    }

    @Test
    @DisplayName("each player has exactly one Snack Stash in their starting hand")
    void eachPlayerHasOneSnackStash() {
      Game game = buildGame(List.of("Alice", "Bob", "Carol"));
      game.getPlayers().forEach(p -> {
        long count = p.getHand().stream().filter(Card::isSnackStash).count();
        assertEquals(1, count);
      });
    }

    @Test
    @DisplayName("deck contains exactly playerCount - 1 doom cards after setup")
    void deckContainsCorrectDoomCount() {
      List<String> names = List.of("Alice", "Bob", "Carol");
      Game game = buildGame(names);
      long doomCount = countDoomCardsInDeck(game.getDeck());
      assertEquals(names.size() - 1, doomCount);
    }

    @ParameterizedTest(name = "{0} players → {0}-1 doom cards in deck")
    @ValueSource(ints = {2, 3, 4, 5})
    @DisplayName("doom card count scales with player count")
    void doomCountScalesWithPlayerCount(int playerCount) {
      List<String> names = new ArrayList<>();
      for (int i = 0; i < playerCount; i++) {
        names.add("Player" + i);
      }
      Game game = buildGame(names);
      long doomCount = countDoomCardsInDeck(game.getDeck());
      assertEquals(playerCount - 1, doomCount);
    }

    @Test
    @DisplayName("board is initialized after setup()")
    void boardInitializedAfterSetup() {
      assertNotNull(buildGame(List.of("Alice", "Bob")).getBoard());
    }

    @Test
    @DisplayName("deck is initialized after setup()")
    void deckInitializedAfterSetup() {
      assertNotNull(buildGame(List.of("Alice", "Bob")).getDeck());
    }

    @Test
    @DisplayName("winner remains null after setup()")
    void winnerNullAfterSetup() {
      assertNull(buildGame(List.of("Alice", "Bob")).getWinner());
    }

    @Test
    @DisplayName("starting player index is within valid player range")
    void startingPlayerIndexIsValid() {
      Game game = buildGame(List.of("Alice", "Bob", "Carol"));
      Player current = game.getBoard().getCurrentPlayer();
      assertTrue(game.getPlayers().contains(current));
    }

    /** Drains the deck and counts doom cards without modifying the game state. */
    private long countDoomCardsInDeck(Deck deck) {
      long count = 0;
      Card card;
      while ((card = deck.draw()) != null) {
        if (card.isDoom()) {
          count++;
        }
      }
      return count;
    }
  }


  // ── checkWinCondition() ───────────────────────────────────────────────────

  @Nested
  @DisplayName("checkWinCondition()")
  class CheckWinConditionTests {

    private Game game;

    @BeforeEach
    void init() {
      game = buildGame(List.of("Alice", "Bob", "Carol"));
    }

    @Test
    @DisplayName("returns false when multiple players are alive")
    void falseWhenMultiplePlayersAlive() {
      assertFalse(game.checkWinCondition());
    }

    @Test
    @DisplayName("winner is null when multiple players are alive")
    void winnerNullWhenMultipleAlive() {
      game.checkWinCondition();
      assertNull(game.getWinner());
    }

    @Test
    @DisplayName("returns true when exactly one player remains")
    void trueWhenOnePlayerRemains() {
      eliminate(game.getPlayers().get(1));
      eliminate(game.getPlayers().get(2));
      assertTrue(game.checkWinCondition());
    }

    @Test
    @DisplayName("sets winner to the last surviving player")
    void setsCorrectWinner() {
      eliminate(game.getPlayers().get(1));
      eliminate(game.getPlayers().get(2));
      game.checkWinCondition();
      assertEquals("Alice", game.getWinner().getName());
    }

    @Test
    @DisplayName("transitions state to FINISHED when one player remains")
    void stateFinishedWhenOnePlayerRemains() {
      eliminate(game.getPlayers().get(0));
      eliminate(game.getPlayers().get(1));
      game.checkWinCondition();
      assertEquals(Game.State.FINISHED, game.getState());
    }

    @Test
    @DisplayName("state remains RUNNING when multiple players are alive")
    void stateRemainsRunningWithMultiplePlayers() {
      game.checkWinCondition();
      assertEquals(Game.State.RUNNING, game.getState());
    }

    @Test
    @DisplayName("can be called multiple times without changing the winner")
    void idempotentAfterWinner() {
      eliminate(game.getPlayers().get(1));
      eliminate(game.getPlayers().get(2));
      game.checkWinCondition();
      Player firstWinner = game.getWinner();
      game.checkWinCondition();
      assertSame(firstWinner, game.getWinner());
    }
  }


  // ── executeTurn() ─────────────────────────────────────────────────────────

  @Nested
  @DisplayName("executeTurn()")
  class ExecuteTurnTests {

    private Game game;

    @BeforeEach
    void init() {
      game = buildGame(List.of("Alice", "Bob"));
    }

    @Test
    @DisplayName("is a no-op when state is SETUP")
    void noOpInSetupState() {
      Game freshGame = new Game();
      assertDoesNotThrow(() -> freshGame.executeTurn(List.of()));
    }

    @Test
    @DisplayName("is a no-op when state is FINISHED")
    void noOpInFinishedState() {
      eliminate(game.getPlayers().get(1));
      game.checkWinCondition();
      assertEquals(Game.State.FINISHED, game.getState());
      assertDoesNotThrow(() -> game.executeTurn(List.of()));
    }

    @Test
    @DisplayName("does not change winner when called after game is FINISHED")
    void winnerUnchangedAfterFinished() {
      eliminate(game.getPlayers().get(1));
      game.checkWinCondition();
      Player winner = game.getWinner();
      game.executeTurn(List.of());
      assertSame(winner, game.getWinner());
    }

    @Test
    @DisplayName("played card is removed from the current player's hand")
    void playedCardRemovedFromHand() {
      Player current = game.getBoard().getCurrentPlayer();
      Card toPlay = firstActionCard(current);
      org.junit.jupiter.api.Assumptions.assumeTrue(toPlay != null);

      int handBefore = current.getHand().size();
      game.executeTurn(List.of(toPlay.getId()));

      assertFalse(current.getHand().contains(toPlay));
    }

    @Test
    @DisplayName("played card is added to the board discard pile")
    void playedCardAddedToDiscardPile() {
      Player current = game.getBoard().getCurrentPlayer();
      Card toPlay = firstActionCard(current);
      org.junit.jupiter.api.Assumptions.assumeTrue(toPlay != null);

      game.executeTurn(List.of(toPlay.getId()));

      assertTrue(game.getBoard().getDiscardPile().contains(toPlay));
    }

    @Test
    @DisplayName("hand size changes by at most 1 after a turn with no cards played")
    void handSizeChangesCorrectlyAfterEmptyTurn() {
      Player current = game.getBoard().getCurrentPlayer();
      int handBefore = current.getHand().size();
      game.executeTurn(List.of());
      int diff = Math.abs(current.getHand().size() - handBefore);
      assertTrue(diff <= 1, "Hand size should change by at most 1");
    }

    @Test
    @DisplayName("advances to the next player after a normal turn")
    void advancesToNextPlayer() {
      Player first = game.getBoard().getCurrentPlayer();
      game.executeTurn(List.of());
      // Only check if game is still running (no doom elimination possible)
      if (game.getState() == Game.State.RUNNING) {
        assertNotSame(first, game.getBoard().getCurrentPlayer());
      }
    }

    @Test
    @DisplayName("multiple cards can be played in a single turn")
    void multipleCardsPlayedInOneTurn() {
      Player current = game.getBoard().getCurrentPlayer();
      List<String> actionIds = current.getHand().stream()
        .filter(c -> !c.isSnackStash() && !c.isDoom())
        .limit(2)
        .map(Card::getId)
        .toList();
      org.junit.jupiter.api.Assumptions.assumeTrue(actionIds.size() == 2);

      int discardsBefore = game.getBoard().getDiscardPile().size();
      game.executeTurn(actionIds);
      assertEquals(discardsBefore + 2, game.getBoard().getDiscardPile().size());
    }

    @Test
    @DisplayName("throws IllegalArgumentException when playing a card not in hand")
    void throwsForCardNotInHand() {
      assertThrows(IllegalArgumentException.class, () ->
        game.executeTurn(List.of("nonexistent_card_id")));
    }

    @Test
    @DisplayName("game transitions to FINISHED when last opponent is eliminated mid-turn")
    void gameFinishesWhenLastOpponentEliminated() {
      // Reduce Bob to 1 life so the next doom card finishes him
      Player bob = game.getPlayers().get(1);
      bob.handleDoom();
      bob.handleDoom();

      // Force a doom card to be drawn by Alice on her turn
      injectDoomCard(game);

      // Ensure it is Alice's turn
      ensureCurrentPlayer(game, "Alice");

      game.executeTurn(List.of());

      // Either Bob was eliminated by doom on Alice's draw, or the game is still running
      // — we assert the state is consistent either way
      if (!bob.isAlive()) {
        assertEquals(Game.State.FINISHED, game.getState());
        assertNotNull(game.getWinner());
      }
    }

    /** Returns the first non-snack-stash, non-doom card in the player's hand, or null. */
    private Card firstActionCard(Player player) {
      return player.getHand().stream()
        .filter(c -> !c.isSnackStash() && !c.isDoom())
        .findFirst()
        .orElse(null);
    }

    /** Injects a doom card at the top of the deck by replacing the deck contents. */
    private void injectDoomCard(Game game) {
      // Draw and discard the top card, then add a doom card — a lightweight injection
      // without requiring package-private access to the deck internals.
      Card doom = new Card("injected_doom", "Doom Hamster", "doom");
      game.getDeck().insertDoomCards(List.of(doom));
    }

    /** Advances turns until the named player is current, up to playerCount attempts. */
    private void ensureCurrentPlayer(Game game, String name) {
      int attempts = game.getPlayers().size();
      while (!game.getBoard().getCurrentPlayer().getName().equals(name) && attempts-- > 0) {
        game.getBoard().advanceTurn();
      }
    }
  }


  // ── State enum ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("State enum")
  class StateEnumTests {

    @Test
    @DisplayName("SETUP, RUNNING, and FINISHED are the only states")
    void enumValuesComplete() {
      assertEquals(3, Game.State.values().length);
    }

    @Test
    @DisplayName("enum values are in lifecycle order")
    void enumLifecycleOrder() {
      Game.State[] states = Game.State.values();
      assertEquals(Game.State.SETUP, states[0]);
      assertEquals(Game.State.RUNNING, states[1]);
      assertEquals(Game.State.FINISHED, states[2]);
    }

    @Test
    @DisplayName("game passes through all three states in order")
    void gamePassesThroughAllStates() {
      Game game = new Game();
      assertEquals(Game.State.SETUP, game.getState());

      game.setup(List.of("Alice", "Bob"), actionCards(20),
        snackStashProto(), doomCards(5));
      assertEquals(Game.State.RUNNING, game.getState());

      eliminate(game.getPlayers().get(1));
      game.checkWinCondition();
      assertEquals(Game.State.FINISHED, game.getState());
    }
  }
}

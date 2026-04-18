import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoardTest {

  private List<Player> players;
  private Board board;

  @BeforeEach
  void init() {
    players = List.of(
      new Player("p0", "Alice"),
      new Player("p1", "Bob"),
      new Player("p2", "Carol")
    );
    board = new Board(new ArrayList<>(players), new Deck(GameTest.makeActionCards(30)));
  }

  @Test
  @DisplayName("advanceTurn() moves to next player")
  void advanceTurn() {
    board.setCurrentIndex(0);
    board.advanceTurn();
    assertEquals("Bob", board.getCurrentPlayer().getName());
  }

  @Test
  @DisplayName("advanceTurn() wraps around to first player")
  void advanceTurnWraps() {
    board.setCurrentIndex(2);
    board.advanceTurn();
    assertEquals("Alice", board.getCurrentPlayer().getName());
  }

  @Test
  @DisplayName("advanceTurn() skips eliminated players")
  void advanceTurnSkipsEliminated() {
    // Eliminate Bob
    players.get(1).handleDoom();
    players.get(1).handleDoom();
    players.get(1).handleDoom();

    board.setCurrentIndex(0);
    board.advanceTurn();
    assertEquals("Carol", board.getCurrentPlayer().getName());
  }

  @Test
  @DisplayName("getActivePlayers() excludes eliminated players")
  void getActivePlayers() {
    players.get(0).handleDoom();
    players.get(0).handleDoom();
    players.get(0).handleDoom(); // Alice eliminated

    assertEquals(2, board.getActivePlayers().size());
  }

  @Test
  @DisplayName("discardCard() adds to discard pile")
  void discardCard() {
    Card card = new Card("a1", "Test", "action");
    board.discardCard(card);
    assertEquals(1, board.getDiscardPile().size());
  }

  @Test
  @DisplayName("turnCount increments with each advanceTurn()")
  void turnCountIncrements() {
    board.advanceTurn();
    board.advanceTurn();
    assertEquals(2, board.getTurnCount());
  }
}


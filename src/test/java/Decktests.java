import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Decktests {



  @Test
  @DisplayName("draw() returns null on empty deck")
  void drawEmptyDeck() {
    Deck deck = new Deck();
    assertNull(deck.draw());
  }

  @Test
  @DisplayName("draw() reduces deck size by 1")
  void drawReducesSize() {
    Deck deck = new Deck(GameTest.makeActionCards(5));
    deck.draw();
    assertEquals(4, deck.size());
  }

  @Test
  @DisplayName("drawMultiple() returns correct count")
  void drawMultiple() {
    Deck deck = new Deck(GameTest.makeActionCards(10));
    List<Card> drawn = deck.drawMultiple(4);
    assertEquals(4, drawn.size());
    assertEquals(6, deck.size());
  }

  @Test
  @DisplayName("drawMultiple() stops early when deck runs out")
  void drawMultipleExceedsSize() {
    Deck deck = new Deck(GameTest.makeActionCards(3));
    List<Card> drawn = deck.drawMultiple(10);
    assertEquals(3, drawn.size());
    assertTrue(deck.isEmpty());
  }

  @Test
  @DisplayName("shuffle() keeps all cards in deck")
  void shuffleKeepsAllCards() {
    Deck deck = new Deck(GameTest.makeActionCards(20));
    deck.shuffle();
    assertEquals(20, deck.size());
  }

  @Test
  @DisplayName("discard() adds card to discard pile")
  void discardAddsToDiscards() {
    Deck deck = new Deck(GameTest.makeActionCards(3));
    Card card = deck.draw();
    deck.discard(card);
    assertEquals(1, deck.getDiscards().size());
  }

  @Test
  @DisplayName("insertDoomCards() increases deck size correctly")
  void insertDoomCards() {
    Deck deck = new Deck(GameTest.makeActionCards(10));
    deck.insertDoomCards(GameTest.makeDoomCards(3));
    assertEquals(13, deck.size());
  }

  @Test
  @DisplayName("insertDoomCards() actually places doom cards in deck")
  void insertDoomCardsAreDoom() {
    Deck deck = new Deck(GameTest.makeActionCards(10));
    deck.insertDoomCards(GameTest.makeDoomCards(2));

    long doomCount = 0;
    for (int i = 0; i < 12; i++) {
      Card c = deck.draw();
      if (c != null && c.isDoom()) doomCount++;
    }
    assertEquals(2, doomCount);
  }
}



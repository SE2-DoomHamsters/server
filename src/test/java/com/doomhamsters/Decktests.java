package com.doomhamsters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class Decktests {
  static List<Card> makeActionCards(int count) {
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      cards.add(new Card("action_" + i, "Action Card " + i, "action"));
    }
    return cards;
  }

  private static Card makeSnackStash() {
    return new Card("ss_proto", "Snack Stash", "snack_stash");
  }

  private static List<Card> makeDoomCards(int count) {
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      cards.add(new Card("doom_" + i, "Doom Hamster " + i, "doom"));
    }
    return cards;
  }

  @Test
  @DisplayName("draw() returns null on empty deck")
  void drawEmptyDeck() {
    Deck deck = new Deck();
    assertNull(deck.draw());
  }

  @Test
  @DisplayName("draw() reduces deck size by 1")
  void drawReducesSize() {
    Deck deck = new Deck(makeActionCards(5));
    deck.draw();
    assertEquals(4, deck.size());
  }

  @Test
  @DisplayName("drawMultiple() returns correct count")
  void drawMultiple() {
    Deck deck = new Deck(makeActionCards(10));
    List<Card> drawn = deck.drawMultiple(4);
    assertEquals(4, drawn.size());
    assertEquals(6, deck.size());
  }

  @Test
  @DisplayName("drawMultiple() stops early when deck runs out")
  void drawMultipleExceedsSize() {
    Deck deck = new Deck(makeActionCards(3));
    List<Card> drawn = deck.drawMultiple(10);
    assertEquals(3, drawn.size());
    assertTrue(deck.isEmpty());
  }

  @Test
  @DisplayName("shuffle() keeps all cards in deck")
  void shuffleKeepsAllCards() {
    Deck deck = new Deck(makeActionCards(20));
    deck.shuffle();
    assertEquals(20, deck.size());
  }

  @Test
  @DisplayName("discard() adds card to discard pile")
  void discardAddsToDiscards() {
    Deck deck = new Deck(makeActionCards(3));
    Card card = deck.draw();
    deck.discard(card);
    assertEquals(1, deck.getDiscards().size());
  }

  @Test
  @DisplayName("insertDoomCards() increases deck size correctly")
  void insertDoomCards() {
    Deck deck = new Deck(makeActionCards(10));
    deck.insertDoomCards(makeDoomCards(3));
    assertEquals(13, deck.size());
  }

  @Test
  @DisplayName("insertDoomCards() actually places doom cards in deck")
  void insertDoomCardsAreDoom() {
    Deck deck = new Deck(makeActionCards(10));
    deck.insertDoomCards(makeDoomCards(2));

    long doomCount = 0;
    for (int i = 0; i < 12; i++) {
      Card c = deck.draw();
      if (c != null && c.isDoom()) {
        doomCount++;
      }
    }
    assertEquals(2, doomCount);
  }
  @Test
  @DisplayName("Copy constructor creates a deep copy")
  void copyConstructor() {
    Deck original = new Deck(makeActionCards(2));
    original.discard(new Card("discard1", "Discard", "action"));

    Deck copy = new Deck(original);

    assertEquals(2, copy.size());
    assertEquals(1, copy.getDiscards().size());

    copy.draw();
    assertEquals(2, original.size());
    assertEquals(1, copy.size());
  }

  @Test
  @DisplayName("getCards() returns the remaining cards")
  void getCards() {
    Deck deck = new Deck(makeActionCards(3));
    assertEquals(3, deck.getCards().size());
  }
}



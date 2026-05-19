package com.doomhamsters;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Represents the deck.
 */

public class Deck {

  private static final SecureRandom RANDOM = new SecureRandom();

  private final List<Card> cards;
  private final List<Card> discards;

  /**
   * Creates a deck from the supplied cards.
   *
   * @param cards initial draw pile cards
   */
  public Deck(List<Card> cards) {
    this.cards = new ArrayList<>(cards);
    this.discards = new ArrayList<>();
  }

  /**
   * Creates a deep copy of another deck.
   *
   * @param other the deck to copy
   */
  public Deck(Deck other) {
    this.cards = new ArrayList<>();
    this.discards = new ArrayList<>();

    for (Card card : other.cards) {
      this.cards.add(new Card(card));
    }

    for (Card card : other.discards) {
      this.discards.add(new Card(card));
    }
  }

  /**
   * Default constructor.
   */

  public Deck() {
    this(new ArrayList<>());
  }

  /**
   * Returns whether the draw pile is empty.
   *
   * @return {@code true} when no cards remain in the draw pile
   */
  public boolean isEmpty() {
    return cards.isEmpty();
  }

  /**
   * Returns the number of cards in the draw pile.
   *
   * @return draw pile size
   */
  public int size() {
    return cards.size();
  }

  /**
   * Fisher-Yates-Mischung.
   */

  public void shuffle() {
    Collections.shuffle(cards);
  }

  /**
   * Draws a card.
   *
   * @return the drawn card, or {@code null} when the deck is empty
   */
  public Card draw() {
    if (cards.isEmpty()) {
      return null;
    }
    return cards.remove(0);
  }

  /**
   * Returns an unmodifiable view of the cards currently in the deck.
   *
   * @return the remaining cards
   */
  public List<Card> getCards() {
    return Collections.unmodifiableList(cards);
  }

  /**
   * Zieht n Karten als Liste.
   *
   * @param n maximum number of cards to draw
   * @return cards drawn from the top of the deck
   */
  public List<Card> drawMultiple(int n) {
    List<Card> drawn = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      Card card = draw();
      if (card == null) {
        break;
      }
      drawn.add(card);
    }
    return drawn;
  }

  /**
   * Adds a card to the discard pile.
   *
   * @param card card to discard
   */
  public void discard(Card card) {
    discards.add(card);
  }

  /**
   * Shuffles Doom cards into the existing draw pile.
   *
   * @param doomCards Doom cards to insert
   */
  public void insertDoomCards(List<Card> doomCards) {
    cards.addAll(doomCards);
    shuffle();
  }

  /**
   * Reinserts a Doom card at a random position in the remaining draw deck.
   *
   * @param doomCard Doom card to reinsert
   */
  public void reinsertDoomCard(Card doomCard) {
    int position = RANDOM.nextInt(cards.size() + 1);
    reinsertDoomCard(doomCard, position);
  }

  /**
   * Reinserts a Doom card at the given position in the remaining draw deck.
   *
   * @param doomCard Doom card to reinsert
   * @param position zero-based insertion position
   */
  public void reinsertDoomCard(Card doomCard, int position) {
    if (position < 0 || position > cards.size()) {
      throw new IllegalArgumentException("Doom insertion position is out of range");
    }
    cards.add(position, doomCard);
  }

  /**
   * Returns the discard pile.
   *
   * @return immutable discard pile snapshot
   */
  public List<Card> getDiscards() {
    return Collections.unmodifiableList(discards);
  }
}

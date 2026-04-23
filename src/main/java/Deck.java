import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Represents the deck.
 */

public class Deck {

  private final List<Card> cards;
  private final List<Card> discards;
  /**
   * Constructor for discard deck.
   */

  public Deck(List<Card> cards) {
    this.cards = new ArrayList<>(cards);
    this.discards = new ArrayList<>();
  }
  /**
   * Default constructor.
   */

  public Deck() {
    this(new ArrayList<>());
  }

  public boolean isEmpty() {
    return cards.isEmpty();
  }
  /**
   * Helper method that returns the size.
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
   * Legt eine Karte auf den Ablagestapel.
   */
  public void discard(Card card) {
    discards.add(card);
  }

  /**
   * Mischt Doom-Karten in den bestehenden Stapel ein.
   */
  public void insertDoomCards(List<Card> doomCards) {
    cards.addAll(doomCards);
    shuffle();
  }

  public List<Card> getDiscards() {
    return Collections.unmodifiableList(discards);
  }
}

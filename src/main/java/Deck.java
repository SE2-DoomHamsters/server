import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
  private final List<Card> cards;
  private final List<Card> discards;

  public Deck(List<Card> cards) {
    this.cards    = new ArrayList<>(cards);
    this.discards = new ArrayList<>();
  }

  public Deck() {
    this(new ArrayList<>());
  }

  public boolean isEmpty() { return cards.isEmpty(); }
  public int size()        { return cards.size(); }

  /** Fisher-Yates-Mischung */
  public void shuffle() {
    Collections.shuffle(cards);
  }

  public Card draw() {
    if (cards.isEmpty()) return null;
    return cards.remove(0);
  }

  /** Zieht n Karten als Liste */
  public List<Card> drawMultiple(int n) {
    List<Card> drawn = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      Card card = draw();
      if (card == null) break;
      drawn.add(card);
    }
    return drawn;
  }

  /** Legt eine Karte auf den Ablagestapel */
  public void discard(Card card) {
    discards.add(card);
  }

  /** Mischt Doom-Karten in den bestehenden Stapel ein */
  public void insertDoomCards(List<Card> doomCards) {
    cards.addAll(doomCards);
    shuffle();
  }

  public List<Card> getDiscards() { return discards; }
}

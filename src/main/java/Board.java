import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Represents the game board and manages its state.
 */

public class Board {

  private final List<Player> players;
  private final Deck deck;
  private final List<Card> discardPile;
  private int currentIndex;
  private int turnCount;
  /**
   * Constructor.
   */

  public Board(List<Player> players, Deck deck) {
    this.players = new ArrayList<>(players);
    this.deck = new Deck(new ArrayList<>(deck.getCards()));
    this.currentIndex = 0;
    this.turnCount = 0;
    this.discardPile = new ArrayList<>();
  }

  public Player getCurrentPlayer() {
    return players.get(currentIndex);
  }

  /**
   * Returns a list of all players who have not yet been eliminated.
   *
   * @return an unmodifiable list of living {@link Player} instances; never {@code null}
   */

  public List<Player> getActivePlayers() {
    return players.stream()
      .filter(Player::isAlive)
      .toList();
  }


  /**
   * Returns a defensive copy of the current deck.
   *
   * <p>Mutations to the returned deck do not affect the game's internal state.
   *
   * @return a copy of the {@link Deck}
   */
  public Deck getDeck() {
    return new Deck(new ArrayList<>(deck.getCards()));
  }


  public int getTurnCount() {
    return turnCount;
  }

  public List<Card> getDiscardPile() {
    return Collections.unmodifiableList(discardPile);
  }
  /**
   * Advances turn.
   */

  public void advanceTurn() {
    turnCount++;
    do {
      currentIndex = (currentIndex + 1) % players.size();
    } while (!players.get(currentIndex).isAlive());
  }

  /**
   * Legt eine gespielte Karte auf den Board-Ablagestapel.
   */
  public void discardCard(Card card) {
    discardPile.add(card);
  }

  public void setCurrentIndex(int index) {
    this.currentIndex = index;
  }
}

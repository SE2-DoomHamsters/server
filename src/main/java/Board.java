import java.util.ArrayList;
import java.util.List;

public class Board {

  private final List<Player> players;
  private final Deck deck;
  private final List<Card> discardPile;
  private int currentIndex;
  private int turnCount;

  public Board(List<Player> players, Deck deck) {
    this.players = players;
    this.deck = deck;
    this.currentIndex = 0;
    this.turnCount = 0;
    this.discardPile = new ArrayList<>();
  }

  public Player getCurrentPlayer() {
    return players.get(currentIndex);
  }

  public List<Player> getActivePlayers() {
    return players.stream()
        .filter(Player::isAlive)
        .toList();
  }

  public Deck getDeck() {
    return deck;
  }

  public int getTurnCount() {
    return turnCount;
  }

  public List<Card> getDiscardPile() {
    return discardPile;
  }

  public void advanceTurn() {
    turnCount++;
    do {
      currentIndex = (currentIndex + 1) % players.size();
    } while (!players.get(currentIndex).isAlive());
  }

  /**
   * Legt eine gespielte Karte auf den Board-Ablagestapel
   */
  public void discardCard(Card card) {
    discardPile.add(card);
  }

  public void setCurrentIndex(int index) {
    this.currentIndex = index;
  }
}

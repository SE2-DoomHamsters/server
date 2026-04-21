import java.util.ArrayList;
import java.util.List;

public class Player {
  public static final int STARTING_LIVES = 3;

  private final String id;
  private final String name;
  private int lives;
  private final List<Card> hand;
  private boolean eliminated;

  public Player(String id, String name) {
    this.id         = id;
    this.name       = name;
    this.lives      = STARTING_LIVES;
    this.hand       = new ArrayList<>();
    this.eliminated = false;
  }

  public String getId()   { return id; }
  public String getName() { return name; }
  public int getLives()   { return lives; }
  public List<Card> getHand() { return hand; }

  public boolean isAlive()      { return lives > 0 && !eliminated; }
  public boolean isEliminated() { return eliminated; }

  public boolean hasSnackStash() {
    return hand.stream().anyMatch(Card::isSnackStash);
  }

  public void addToHand(Card card) {
    hand.add(card);
  }

  /** Entfernt eine Karte aus der Hand und gibt sie zurück (oder null) */
  public Card removeFromHand(String cardId) {
    for (int i = 0; i < hand.size(); i++) {
      if (hand.get(i).getId().equals(cardId)) {
        return hand.remove(i);
      }
    }
    return null;
  }

  /**
   * Verarbeitet eine gezogene Doom-Karte.
   * @return DoomResult mit Info, ob Snack Stash verwendet wurde
   */
  public DoomResult handleDoom() {
    for (int i = 0; i < hand.size(); i++) {
      if (hand.get(i).isSnackStash()) {
        hand.remove(i);
        return new DoomResult(true, lives);
      }
    }
    // Kein Snack Stash → Leben verlieren
    lives--;
    if (lives <= 0) {
      eliminated = true;
    }
    return new DoomResult(false, lives);
  }

  /** Spielt eine Karte aus der Hand */
  public Card playCard(String cardId, Game game) {
    Card card = removeFromHand(cardId);
    if (card == null) {
      throw new IllegalArgumentException(
        "Karte " + cardId + " nicht in der Hand von " + name
      );
    }
    card.play(game, this);
    return card;
  }

  /** Einfaches Result-Objekt für handleDoom() */
  public static class DoomResult {
    public final boolean neutralized;
    public final int livesRemaining;

    public DoomResult(boolean neutralized, int livesRemaining) {
      this.neutralized    = neutralized;
      this.livesRemaining = livesRemaining;
    }
  }
}

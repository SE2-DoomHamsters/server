import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Represents the player.
 */

public class Player {

  public static final int STARTING_LIVES = 3;

  private final String id;
  private final String name;
  private final List<Card> hand;
  private int lives;
  private boolean eliminated;

  /**
   * Constructs the default player.
   */

  public Player(String id, String name) {
    this.id = id;
    this.name = name;
    this.lives = STARTING_LIVES;
    this.hand = new ArrayList<>();
    this.eliminated = false;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getLives() {
    return lives;
  }

  public List<Card> getHand() {
    return Collections.unmodifiableList(hand);
  }

  public boolean isAlive() {
    return lives > 0 && !eliminated;
  }

  public boolean isEliminated() {
    return eliminated;
  }

  /**
   * Returns whether this player has a Snack Stash card in hand.
   *
   * @return {@code true} if at least one Snack Stash card is present in the player's hand
   */
  public boolean hasSnackStash() {
    return hand.stream().anyMatch(Card::isSnackStash);
  }

  /**
   * Adds a card to this player's hand.
   *
   * @param card the card to add
   */
  public void addToHand(Card card) {
    hand.add(card);
  }

  /**
   * Removes and returns the card with the given id from this player's hand.
   *
   * @param cardId the id of the card to remove
   * @return the removed card, or {@code null} if no card with that id was found
   */
  public Card removeFromHand(String cardId) {
    for (int i = 0; i < hand.size(); i++) {
      if (hand.get(i).getId().equals(cardId)) {
        return hand.remove(i);
      }
    }
    return null;
  }

  /**
   * Handles the effect of drawing a Doom Hamster card.
   *
   * <p>If the player holds a Snack Stash, it is consumed and the doom is neutralized. Otherwise
   * the player loses one life and is eliminated if their life count reaches zero.
   *
   * @return a {@link DoomResult} indicating whether the doom was neutralized and the remaining
   *     lives
   */
  public DoomResult handleDoom() {
    for (int i = 0; i < hand.size(); i++) {
      if (hand.get(i).isSnackStash()) {
        hand.remove(i);
        return new DoomResult(true, lives);
      }
    }
    lives--;
    if (lives <= 0) {
      eliminated = true;
    }
    return new DoomResult(false, lives);
  }

  /**
   * Plays the card with the given id from this player's hand.
   *
   * @param cardId the id of the card to play
   * @param game   the current game instance
   * @return the card that was played
   * @throws IllegalArgumentException if no card with the given id is in this player's hand
   */
  public Card playCard(String cardId, Game game) {
    Card card = removeFromHand(cardId);
    if (card == null) {
      throw new IllegalArgumentException(
        "No card with id " + cardId + " in " + name + "'s hand");
    }
    card.play(game, this);
    return card;
  }

  /**
   * Holds the result of a {@link #handleDoom()} call.
   *
   * <p>Indicates whether a Snack Stash was used to neutralize the doom, and how many lives the
   * player has remaining after the event.
   */
  public static class DoomResult {

    /** Whether the Doom Hamster was neutralized by a Snack Stash. */
    public final boolean neutralized;

    /** The number of lives remaining after the doom event. */
    public final int livesRemaining;

    /**
     * Creates a new DoomResult.
     *
     * @param neutralized    {@code true} if a Snack Stash was consumed to block the doom
     * @param livesRemaining the player's remaining life count after the event
     */
    public DoomResult(boolean neutralized, int livesRemaining) {
      this.neutralized = neutralized;
      this.livesRemaining = livesRemaining;
    }
  }
}

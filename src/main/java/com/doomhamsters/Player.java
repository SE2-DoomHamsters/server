package com.doomhamsters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Represents the player.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {

  /** Starting number of lives for each player. */
  public static final int STARTING_LIVES = 3;

  private final String id;
  private final String name;
  private final List<Card> hand;
  private int lives;
  private boolean eliminated;

  /**
   * Creates a new player.
   *
   * @param id stable player identifier
   * @param name display name
   */
  public Player(String id, String name) {
    this.id = id;
    this.name = name;
    this.lives = STARTING_LIVES;
    this.hand = new ArrayList<>();
    this.eliminated = false;
  }

  /**
   * Creates a player from persisted JSON state.
   *
   * @param id stable player identifier
   * @param name display name
   * @param hand cards currently in hand
   * @param lives remaining lives
   * @param eliminated whether the player is eliminated
   */
  @JsonCreator
  public Player(
      @JsonProperty("id") String id,
      @JsonProperty("name") String name,
      @JsonProperty("hand") List<Card> hand,
      @JsonProperty("lives") Integer lives,
      @JsonProperty("eliminated") Boolean eliminated) {
    this.id = id;
    this.name = name;
    this.lives = lives == null ? STARTING_LIVES : lives;
    this.hand = hand == null ? new ArrayList<>() : new ArrayList<>(hand);
    this.eliminated = eliminated != null && eliminated;
  }

  /**
   * Creates a deep copy of another player.
   *
   * @param other the player to copy
   */
  public Player(Player other) {
    this.id = other.id;
    this.name = other.name;
    this.lives = other.lives;
    this.eliminated = other.eliminated;

    this.hand = new ArrayList<>();
    for (Card card : other.hand) {
      this.hand.add(new Card(card));
    }
  }

  /**
   * Returns the stable player identifier.
   *
   * @return player id
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the player display name.
   *
   * @return display name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the player's remaining lives.
   *
   * @return remaining lives
   */
  public int getLives() {
    return lives;
  }

  /**
   * Returns the player's hand.
   *
   * @return immutable hand snapshot
   */
  public List<Card> getHand() {
    return Collections.unmodifiableList(hand);
  }

  /**
   * Returns whether the player can still take turns.
   *
   * @return {@code true} when the player has lives and is not eliminated
   */
  public boolean isAlive() {
    return lives > 0 && !eliminated;
  }

  /**
   * Returns whether the player has been eliminated.
   *
   * @return {@code true} when the player is eliminated
   */
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
  /** Decrements lives by one and marks the player eliminated if lives reach zero. */

  public void decrementLives() {
    if (eliminated) {
      return;
    }
    lives = Math.max(0, lives - 1);
    if (lives == 0) {
      eliminated = true;
    }
  }

  /** Increments lives by one. Only applies if the player is not already eliminated. */
  public void incrementLives() {
    if (eliminated) {
      return;
    }
    lives++;
  }

  /**
   * Adds the specified number of lives to the player.
   * Negative values are ignored.
   *
   * @param lives the number of lives to add; must be non-negative
   */
  public void addLives(int lives) {
    if (lives < 0) {
      return;
    }
    this.lives += lives;
  }
}

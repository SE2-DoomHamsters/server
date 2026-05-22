package com.doomhamsters;

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
  private int currentIndex, turnCount, extraTurns;

  /**
   * Creates a board for the supplied players and deck.
   *
   * @param players players participating in the game
   * @param deck draw deck used by the board
   */
  public Board(List<Player> players, Deck deck) {
    this.players = new ArrayList<>(players);
    this.deck = new Deck(new ArrayList<>(deck.getCards()));
    this.currentIndex = 0;
    this.turnCount = 0;
    this.extraTurns = 0;
    this.discardPile = new ArrayList<>();
  }

  /**
   * Creates a deep copy of another board.
   *
   * @param other the board to copy
   * @param players the copied players to use
   * @param deck the copied deck to use
   */
  public Board(Board other, List<Player> players, Deck deck) {
    this.players = new ArrayList<>(players);
    this.deck = new Deck(deck);
    this.currentIndex = other.currentIndex;
    this.turnCount = other.turnCount;
    this.extraTurns = other.extraTurns;

    this.discardPile = new ArrayList<>();
    for (Card card : other.discardPile) {
      this.discardPile.add(new Card(card));
    }
  }

  /**
   * Returns the player whose turn is currently active.
   *
   * @return current player
   */
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


  /**
   * Returns the number of turns completed on this board.
   *
   * @return completed turn count
   */
  public int getTurnCount() {
    return turnCount;
  }

  /**
   * Returns the board discard pile.
   *
   * @return immutable discard pile snapshot
   */
  public List<Card> getDiscardPile() {
    return Collections.unmodifiableList(discardPile);
  }

  /**
   * Advances the game turn to the next active player.
   *
   * <p>If extra turns are available, one extra turn is consumed and the
   * current player keeps their turn instead of advancing to the next player.
   */
  public void advanceTurn() {
    if (extraTurns > 0) {
      extraTurns--;
      return;
    }

    turnCount++;

    do {
      currentIndex = (currentIndex + 1) % players.size();
    } while (!players.get(currentIndex).isAlive());
  }

  /**
   * Adds a played card to the board discard pile.
   *
   * @param card card to discard
   */
  public void discardCard(Card card) {
    discardPile.add(card);
  }

  /**
   * Sets the index of the current player.
   *
   * @param index zero-based player index
   */
  public void setCurrentIndex(int index) {
    this.currentIndex = index;
  }

  /**
   * Grants the current player an additional turn.
   *
   * <p>Extra turns are stacked and consumed one at a time when
   * {@link #advanceTurn()} is called.
   */
  public void addExtraTurn() {
    extraTurns++;
  }

  /**
   * Returns the number of queued extra turns.
   *
   * @return queued extra turn count
   */
  public int getExtraTurns() {
    return extraTurns;
  }
}

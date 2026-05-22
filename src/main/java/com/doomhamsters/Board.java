package com.doomhamsters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the game board and manages its state.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Board {

  private List<Player> players;
  private Deck deck;
  private final List<Card> discardPile;
  private int currentIndex;
  private int turnCount;
  private int extraTurns;
  private String restoredCurrentPlayerId;

  /**
   * Creates a board for the supplied players and deck.
   *
   * @param players players participating in the game
   * @param deck    draw deck used by the board
   */
  public Board(List<Player> players, Deck deck) {
    this.players = new ArrayList<>(players);
    this.deck = new Deck(new ArrayList<>(deck.getCards()));
    this.currentIndex = 0;
    this.turnCount = 0;
    this.extraTurns = 0;
    this.discardPile = new ArrayList<>();
    this.restoredCurrentPlayerId = null;
  }

  /**
   * Creates a deep copy of another board.
   *
   * @param other   the board to copy
   * @param players the copied players to use
   * @param deck    the copied deck to use
   */
  public Board(Board other, List<Player> players, Deck deck) {
    this.players = new ArrayList<>(players);
    this.deck = new Deck(deck);
    this.currentIndex = other.currentIndex;
    this.turnCount = other.turnCount;
    this.extraTurns = other.extraTurns;
    this.restoredCurrentPlayerId = other.restoredCurrentPlayerId;

    this.discardPile = new ArrayList<>();
    for (Card card : other.discardPile) {
      this.discardPile.add(new Card(card));
    }
  }

  /**
   * Recreates a board from persisted JSON and defers binding the player order until the surrounding
   * game has restored its authoritative player list.
   *
   * @param currentPlayer player whose turn was active when the game was saved
   * @param deck          restored deck snapshot
   * @param discardPile   restored board discard pile
   * @param turnCount     restored turn count
   */
  @JsonCreator
  public Board(
      @JsonProperty("currentPlayer") Player currentPlayer,
      @JsonProperty("deck") Deck deck,
      @JsonProperty("discardPile") List<Card> discardPile,
      @JsonProperty("turnCount") Integer turnCount) {
    this.players = new ArrayList<>();
    this.deck = deck == null ? new Deck() : new Deck(deck);
    this.discardPile = discardPile == null ? new ArrayList<>() : new ArrayList<>(discardPile);
    this.currentIndex = 0;
    this.turnCount = turnCount == null ? 0 : turnCount;
    this.restoredCurrentPlayerId = currentPlayer == null ? null : currentPlayer.getId();
  }

  /**
   * Returns the player whose turn is currently active.
   *
   * @return current player
   */
  public Player getCurrentPlayer() {
    if (players.isEmpty()) {
      return null;
    }
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

  /**
   * Rebinds restored board state to the authoritative game-level players and deck.
   *
   * @param players authoritative game players
   * @param deck    authoritative game deck
   */
  void rebindToGameState(List<Player> players, Deck deck) {
    this.players = new ArrayList<>(players);
    this.deck = deck;

    if (players.isEmpty()) {
      currentIndex = 0;
      return;
    }

    if (restoredCurrentPlayerId == null) {
      currentIndex = Math.max(0, Math.min(currentIndex, players.size() - 1));
      return;
    }

    for (int i = 0; i < players.size(); i++) {
      if (restoredCurrentPlayerId.equals(players.get(i).getId())) {
        currentIndex = i;
        return;
      }
    }

    currentIndex = 0;
  }
}

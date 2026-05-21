package com.doomhamsters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;

/**
 * Orchestrates a full game of Doom Hamster.
 *
 * <p>Manages the game lifecycle from {@link State#SETUP} through {@link State#RUNNING} to {@link
 * State#FINISHED}, coordinating players, the deck, and the board.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Game {

  /** The number of cards each player receives at the start of the game. */
  public static final int STARTING_HAND_SIZE = 6;

  private final SecureRandom random = new SecureRandom();

  private State state;
  private Player winner;
  private String resolvingDoomPlayerId;
  private String pendingDoomCardId;
  private String pendingDoomCardName;
  private String pendingDoomCardType;
  private List<Player> players;
  private Deck deck;
  private Board board;

  /** Creates a new Game instance in the {@link State#SETUP} state. */
  public Game() {
    this.state = State.SETUP;
    this.winner = null;
    this.resolvingDoomPlayerId = null;
    this.pendingDoomCardId = null;
    this.pendingDoomCardName = null;
    this.pendingDoomCardType = null;
    this.players = new ArrayList<>();
  }

  /**
   * Creates a deep copy of another game instance.
   *
   * @param other the game to copy
   */
  public Game(Game other) {
    this.state = other.state;
    this.resolvingDoomPlayerId = other.resolvingDoomPlayerId;
    this.pendingDoomCardId = other.pendingDoomCardId;
    this.pendingDoomCardName = other.pendingDoomCardName;
    this.pendingDoomCardType = other.pendingDoomCardType;

    this.players = new ArrayList<>();
    for (Player player : other.players) {
      this.players.add(new Player(player));
    }

    this.winner =
        other.winner == null
            ? null
            : this.players.stream()
                .filter(p -> p.getId().equals(other.winner.getId()))
                .findFirst()
                .orElse(null);

    this.deck = other.deck == null ? null : new Deck(other.deck);

    this.board =
        other.board == null
            ? null
            : new Board(other.board, this.players, this.deck);
  }

  /**
   * Returns the current state of the game.
   *
   * @return the current {@link State}
   */

  public State getState() {
    return state;
  }

  /**
   * Restores the lifecycle state from persisted JSON.
   *
   * @param state restored state
   */
  public void setState(State state) {
    this.state = state;
  }

  /**
   * Returns the winning player, or {@code null} if the game is not yet finished.
   *
   * @return the winner, or {@code null}
   */

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Player getWinner() {
    return winner;
  }

  /**
   * Restores the winner from persisted JSON.
   *
   * @param winner restored winner
   */
  public void setWinner(Player winner) {
    this.winner = winner == null ? null : new Player(winner);
    rebindRestoredState();
  }

  /**
   * Returns the board managing player order and the discard pile.
   *
   * @return the {@link Board}
   */

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Board getBoard() {
    return board;
  }

  /**
   * Restores the board from persisted JSON.
   *
   * @param board restored board
   */
  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public void setBoard(Board board) {
    this.board = board;
    rebindRestoredState();
  }

  /**
   * Returns the deck currently in use.
   *
   * @return the {@link Deck}
   */

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Deck getDeck() {
    return deck;
  }

  /**
   * Restores the deck from persisted JSON.
   *
   * @param deck restored deck
   */
  public void setDeck(Deck deck) {
    this.deck = deck == null ? null : new Deck(deck);
    rebindRestoredState();
  }

  /**
   * Returns the list of all players, including eliminated ones.
   *
   * @return an unmodifiable view of all players
   */

  public List<Player> getPlayers() {
    return Collections.unmodifiableList(players);
  }

  /**
   * Restores the player list from persisted JSON.
   *
   * @param players restored players
   */
  public void setPlayers(List<Player> players) {
    this.players = new ArrayList<>();
    if (players != null) {
      for (Player player : players) {
        this.players.add(new Player(player));
      }
    }
    rebindRestoredState();
  }

  /**
   * Returns the player currently showing Doom-resolution UI, if any.
   *
   * @return resolving player id, or {@code null}
   */
  public String getResolvingDoomPlayerId() {
    return resolvingDoomPlayerId;
  }

  /**
   * Sets the player currently showing Doom-resolution UI.
   *
   * @param resolvingDoomPlayerId resolving player id, or {@code null}
   */
  public void setResolvingDoomPlayerId(String resolvingDoomPlayerId) {
    this.resolvingDoomPlayerId = resolvingDoomPlayerId;
  }

  /**
   * Clears the pending Doom-resolution UI state.
   */
  public void clearResolvingDoomPlayerId() {
    resolvingDoomPlayerId = null;
  }

  /**
   * Returns whether a neutralized Doom card is waiting to be inserted.
   *
   * @return {@code true} when the resolving player must choose an insertion depth
   */
  public boolean isPendingDoomRequiresInsertion() {
    return pendingDoomCardId != null;
  }

  /**
   * Returns the id of the Doom card waiting to be inserted.
   *
   * @return pending Doom card id, or {@code null}
   */
  public String getPendingDoomCardId() {
    return pendingDoomCardId;
  }

  /**
   * Sets the id of the Doom card waiting to be inserted.
   *
   * @param pendingDoomCardId pending Doom card id, or {@code null}
   */
  public void setPendingDoomCardId(String pendingDoomCardId) {
    this.pendingDoomCardId = pendingDoomCardId;
  }

  /**
   * Returns the name of the Doom card waiting to be inserted.
   *
   * @return pending Doom card name, or {@code null}
   */
  public String getPendingDoomCardName() {
    return pendingDoomCardName;
  }

  /**
   * Sets the name of the Doom card waiting to be inserted.
   *
   * @param pendingDoomCardName pending Doom card name, or {@code null}
   */
  public void setPendingDoomCardName(String pendingDoomCardName) {
    this.pendingDoomCardName = pendingDoomCardName;
  }

  /**
   * Returns the type of the Doom card waiting to be inserted.
   *
   * @return pending Doom card type, or {@code null}
   */
  public String getPendingDoomCardType() {
    return pendingDoomCardType;
  }

  /**
   * Sets the type of the Doom card waiting to be inserted.
   *
   * @param pendingDoomCardType pending Doom card type, or {@code null}
   */
  public void setPendingDoomCardType(String pendingDoomCardType) {
    this.pendingDoomCardType = pendingDoomCardType;
  }

  private void rebindRestoredState() {
    if (board != null && deck != null && players != null) {
      board.rebindToGameState(players, deck);
    }

    if (winner != null && players != null) {
      winner =
          players.stream()
              .filter(player -> player.getId().equals(winner.getId()))
              .findFirst()
              .orElse(winner);
    }
  }

  /**
   * Sets the Doom card waiting to be inserted.
   *
   * @param pendingDoomCard pending Doom card, or {@code null}
   */
  public void setPendingDoomCard(Card pendingDoomCard) {
    if (pendingDoomCard == null) {
      pendingDoomCardId = null;
      pendingDoomCardName = null;
      pendingDoomCardType = null;
      return;
    }

    pendingDoomCardId = pendingDoomCard.getId();
    pendingDoomCardName = pendingDoomCard.getName();
    pendingDoomCardType = pendingDoomCard.getType();
  }

  /**
   * Reinserts the pending Doom card at the requested deck position and clears Doom state.
   *
   * @param position zero-based deck position, where 0 is the top of the deck
   */
  public void insertPendingDoom(int position) {
    if (pendingDoomCardId == null) {
      throw new IllegalStateException("No pending Doom card requires insertion.");
    }

    Card pendingDoomCard =
        new Card(pendingDoomCardId, pendingDoomCardName, pendingDoomCardType);

    deck.reinsertDoomCard(pendingDoomCard, position);
    pendingDoomCardId = null;
    pendingDoomCardName = null;
    pendingDoomCardType = null;
    resolvingDoomPlayerId = null;
  }

  /**
   * Initializes all game entities and transitions the game to {@link State#RUNNING}.
   *
   * <p>Each player receives one Snack Stash card and {@code STARTING_HAND_SIZE - 1} randomly
   * drawn cards from the deck. Doom Hamster cards ({@code playerCount - 1}) are then shuffled into
   * the remaining deck. The starting player is chosen at random.
   *
   * @param playerNames     the display names of the players; must contain at least two entries
   * @param allActionCards  all action cards excluding Doom and Snack Stash cards
   * @param snackStashProto a prototype card used to create each player's Snack Stash
   * @param doomProtos      the pool of Doom Hamster cards to draw from
   * @throws IllegalArgumentException if fewer than two player names are provided
   */

  public void setup(
      List<String> playerNames,
      List<Card> allActionCards,
      Card snackStashProto,
      List<Card> doomProtos) {
    List<String> playerIds = new ArrayList<>();
    for (int i = 0; i < playerNames.size(); i++) {
      playerIds.add("p" + i);
    }
    setupInternal(playerIds, playerNames, allActionCards, snackStashProto, doomProtos);
  }

  /**
   * Initializes a game using stable external player IDs.
   *
   * @param playerIds stable lobby user IDs to preserve in game state
   * @param allActionCards all action cards excluding Doom and Snack Stash cards
   * @param snackStashProto a prototype card used to create each player's Snack Stash
   * @param doomProtos the pool of Doom Hamster cards to draw from
   */
  public void setupWithPlayerIds(
      List<String> playerIds,
      List<Card> allActionCards,
      Card snackStashProto,
      List<Card> doomProtos) {
    setupInternal(playerIds, playerIds, allActionCards, snackStashProto, doomProtos);
  }

  /**
   * Initializes a game using stable external player IDs and separate display names.
   *
   * @param playerIds stable lobby user IDs to preserve in game state
   * @param playerNames display names to show in the client UI
   * @param allActionCards all action cards excluding Doom and Snack Stash cards
   * @param snackStashProto a prototype card used to create each player's Snack Stash
   * @param doomProtos the pool of Doom Hamster cards to draw from
   */
  public void setupWithPlayers(
      List<String> playerIds,
      List<String> playerNames,
      List<Card> allActionCards,
      Card snackStashProto,
      List<Card> doomProtos) {
    setupInternal(playerIds, playerNames, allActionCards, snackStashProto, doomProtos);
  }

  private void setupInternal(
      List<String> playerIds,
      List<String> playerNames,
      List<Card> allActionCards,
      Card snackStashProto,
      List<Card> doomProtos) {
    if (playerNames.size() < 2) {
      throw new IllegalArgumentException("At least 2 players are required");
    }
    if (playerIds.size() != playerNames.size()) {
      throw new IllegalArgumentException("Player IDs and names must have the same size");
    }
    int doomCount = playerNames.size() - 1;
    if (doomProtos.size() < doomCount) {
      throw new IllegalArgumentException("Not enough Doom cards for player count");
    }

    players = new ArrayList<>();
    for (int i = 0; i < playerNames.size(); i++) {
      players.add(new Player(playerIds.get(i), playerNames.get(i)));
    }

    deck = new Deck(allActionCards);
    deck.shuffle();

    for (Player player : players) {
      Card snackStash =
          new Card("ss_" + player.getId(), snackStashProto.getName(), "snack_stash");
      player.addToHand(snackStash);

      List<Card> drawn = deck.drawMultiple(STARTING_HAND_SIZE - 1);
      drawn.forEach(player::addToHand);
    }

    List<Card> doomCards = doomProtos.subList(0, doomCount);
    deck.insertDoomCards(doomCards);

    board = new Board(players, deck);
    board.setCurrentIndex(random.nextInt(players.size()));

    state = State.RUNNING;
  }

  /**
   * Checks whether the win condition has been met and updates the game state accordingly.
   *
   * <p>The game is won when exactly one player with more than zero lives remains. If that
   * condition is met, {@link #getWinner()} will return that player and the state transitions to
   * {@link State#FINISHED}.
   *
   * @return {@code true} if the game has ended and a winner has been determined
   */

  public boolean checkWinCondition() {
    List<Player> alive = board.getActivePlayers();
    if (alive.size() == 1) {
      winner = alive.get(0);
      state = State.FINISHED;
      return true;
    }
    return false;
  }

  /**
   * Executes a full turn for the current player.
   *
   * <p>The turn proceeds in the following order:
   *
   * <ol>
   *   <li>Play any number of cards from the current player's hand (may be empty).
   *   <li>Draw one card from the deck (mandatory).
   *   <li>If the drawn card is a Doom Hamster, resolve its effect via {@link
   *       Player#handleDoom()}.
   *   <li>Check the win condition; end the turn early if the game is over.
   *   <li>Advance to the next living player.
   * </ol>
   *
   * <p>This method is a no-op if the game is not in {@link State#RUNNING}.
   *
   * @param cardIds the ids of cards to play this turn, in order; may be empty
   */

  public void executeTurn(List<String> cardIds) {
    if (state != State.RUNNING) {
      return;
    }

    Player player = board.getCurrentPlayer();

    for (String id : cardIds) {
      Card played = player.playCard(id, this);
      board.discardCard(played);
    }

    Card drawn = deck.draw();
    if (drawn == null) {
      return;
    }

    if (drawn.isDoom()) {
      Player.DoomResult result = player.handleDoom();
      if (result.neutralized) {
        setPendingDoomCard(drawn);
      } else {
        deck.insertDoomCards(List.of(drawn));
      }
      resolvingDoomPlayerId = player.getId();
    } else {
      player.addToHand(drawn);
    }

    if (checkWinCondition()) {
      return;
    }

    if (resolvingDoomPlayerId != null) {
      return;
    }

    board.advanceTurn();
  }

  /**
   * Draws one card for the current player without advancing the turn.
   *
   * <p>This supports clients that send draw and next-turn as separate commands.
   *
   * @return {@code true} if a card was drawn
   */
  public boolean drawForCurrentPlayer() {
    return drawForCurrentPlayerWithResult().cardDrawn();
  }

  /**
   * Draws one card for the current player and returns details about the draw.
   *
   * <p>This supports transports that need to emit draw-specific events in addition to the shared
   * game-state update.
   *
   * @return draw result containing the drawn card, or an empty result if no card was drawn
   */
  public DrawResult drawForCurrentPlayerWithResult() {
    if (state != State.RUNNING) {
      return DrawResult.noCard();
    }

    Player player = board.getCurrentPlayer();
    Card drawn = deck.draw();
    if (drawn == null) {
      return DrawResult.noCard();
    }

    Player.DoomResult doomResult = null;
    if (drawn.isDoom()) {
      doomResult = player.handleDoom();
      if (doomResult.neutralized) {
        setPendingDoomCard(drawn);
      } else {
        deck.insertDoomCards(List.of(drawn));
      }
      resolvingDoomPlayerId = player.getId();
    } else {
      player.addToHand(drawn);
    }

    checkWinCondition();

    return new DrawResult(drawn, doomResult);
  }

  /**
   * Advances to the next active player.
   */
  public void advanceTurn() {
    if (state != State.RUNNING) {
      return;
    }

    if (resolvingDoomPlayerId != null || pendingDoomCardId != null) {
      throw new IllegalStateException("Doom resolution must be acknowledged before next turn.");
    }

    if (checkWinCondition()) {
      return;
    }

    board.advanceTurn();
  }

  /** Represents the lifecycle state of a {@link Game}. */
  public enum State {

    /** The game has been created but {@link Game#setup} has not yet been called. */
    SETUP,

    /** The game is in progress and players are taking turns. */
    RUNNING,

    /** The game has ended and a winner has been determined. */
    FINISHED
  }

  /**
   * Details about a draw action.
   */
  public static class DrawResult {

    private final Card drawnCard;
    private final Player.DoomResult doomResult;

    private DrawResult(
        Card drawnCard,
        Player.DoomResult doomResult) {

      this.drawnCard = drawnCard == null ? null : new Card(drawnCard);
      this.doomResult = doomResult;
    }

    private static DrawResult noCard() {
      return new DrawResult(null, null);
    }

    /**
     * Returns whether a card has been drawn in the current turn.
     *
     * @return {@code true} if a card was drawn, {@code false} if the turn has not produced a draw
     */
    public boolean cardDrawn() {
      return drawnCard != null;
    }

    /**
     * Returns whether the drawn card is a {@link CardType#DOOM} card.
     *
     * @return {@code true} if a card was drawn and it is of type DOOM, {@code false} otherwise
     */
    public boolean doomDrawn() {
      return drawnCard != null && drawnCard.isDoom();
    }

    /**
     * Returns the card that was drawn.
     *
     * @return copy of the drawn card, or {@code null} if no card was drawn
     */
    public Card getDrawnCard() {
      return drawnCard == null ? null : new Card(drawnCard);
    }

    /**
     * Returns the Doom resolution result for the draw.
     *
     * @return Doom result, or {@code null} when no Doom card was drawn
     */
    public Player.DoomResult getDoomResult() {
      return doomResult;
    }
  }
}

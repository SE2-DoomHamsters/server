import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;

/**
 * Orchestrates a full game of Doom Hamster.
 *
 * <p>Manages the game lifecycle from {@link State#SETUP} through {@link State#RUNNING} to {@link
 * State#FINISHED}, coordinating players, the deck, and the board.
 */

public class Game {

  /** The number of cards each player receives at the start of the game. */
  public static final int STARTING_HAND_SIZE = 6;

  private final Random random = new Random();

  private State state;
  private Player winner;
  private List<Player> players;
  private Deck deck;
  private Board board;

  /** Creates a new Game instance in the {@link State#SETUP} state. */
  public Game() {
    this.state = State.SETUP;
    this.winner = null;
    this.players = new ArrayList<>();
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
   * Returns the winning player, or {@code null} if the game is not yet finished.
   *
   * @return the winner, or {@code null}
   */

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Player getWinner() {
    return winner;
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
   * Returns the deck currently in use.
   *
   * @return the {@link Deck}
   */

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Deck getDeck() {
    return deck;
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
   * Initializes all game entities and transitions the game to {@link State#RUNNING}.
   *
   * <p>Each player receives one Snack Stash card and {@code STARTING_HAND_SIZE - 1} randomly
   * drawn action cards. Doom Hamster cards ({@code playerCount - 1}) are then shuffled into the
   * remaining deck. The starting player is chosen at random.
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
    if (playerNames.size() < 2) {
      throw new IllegalArgumentException("At least 2 players are required");
    }

    players = new ArrayList<>();
    for (int i = 0; i < playerNames.size(); i++) {
      players.add(new Player("p" + i, playerNames.get(i)));
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

    int doomCount = playerNames.size() - 1;
    List<Card> doomCards = doomProtos.subList(0, Math.min(doomCount, doomProtos.size()));
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
      if (!result.neutralized) {
        deck.discard(drawn);
      }
    } else {
      player.addToHand(drawn);
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
}

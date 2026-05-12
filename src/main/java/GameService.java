

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;


  /**
   * Manages active game sessions and encapsulates all draw logic.
   *
   * <p>All mutating methods are {@code synchronized} per game ID to prevent
   * race conditions when multiple clients send messages concurrently.
   */
  @Service
  public class GameService {

    /** Live game states keyed by game ID. In production replace with a distributed store. */
    private final ConcurrentHashMap<String, GameState> activeGames = new ConcurrentHashMap<>();

    /** Deck simulation — replace with a real shuffled deck per game session. */
    private final Random random = new Random();

    /**
     * Returns the current {@link GameState} for the given game, or empty if not found.
     *
     * @param gameId the game to look up
     * @return an {@link Optional} wrapping the state
     */
    public Optional<GameState> findGame(String gameId) {
      return Optional.ofNullable(activeGames.get(gameId));
    }

    /**
     * Registers a new game session.
     *
     * @param gameId  unique identifier for the game
     * @param players initial roster of players
     */
    public void registerGame(String gameId, List<Player> players) {
      String firstTurn = players.isEmpty() ? "" : players.get(0).getId();
      activeGames.put(
        gameId,
        new GameState(gameId, players, firstTurn, /* snackStashPending= */ false, /* pendingPlayerId= */ null));
    }

    /**
     * Processes a draw action for {@code playerId} in {@code gameId}.
     *
     * <p>Turn validation is performed first; if the requesting player is not the active player an
     * {@link IllegalArgumentException} is thrown and <em>no state is mutated</em>.
     *
     * <p><b>Card effects:</b>
     * <ul>
     *   <li>{@link Card#DOOM} — lives {@code -1}; player eliminated when lives reach {@code 0}.
     *   <li>{@link Card#SNACK_STASH} — flagged as pending; the client must confirm via
     *       {@code /app/game/{gameId}/confirm-snack} before the bonus is applied.
     *   <li>{@link Card#NORMAL} — no effect; turn advances.
     * </ul>
     *
     * @param gameId   the target game session
     * @param playerId the player attempting to draw
     * @return the updated {@link GameState} ready for broadcast
     * @throws NoSuchElementException   if {@code gameId} is not found
     * @throws IllegalArgumentException if it is not {@code playerId}'s turn
     */
    public synchronized GameState processDraw(String gameId, String playerId) {
      GameState state =
        findGame(gameId)
          .orElseThrow(() -> new NoSuchElementException("Game not found: " + gameId));

      validateTurn(state, playerId);

      List<Player> mutablePlayers = new ArrayList<>(state.getPlayers());
      Card drawnCard = drawCard();

      boolean snackStashPending = false;
      String pendingPlayerId = null;
      String nextTurnPlayerId = state.getCurrentTurnPlayerId();

      switch (drawnCard) {
        case DOOM -> {
          applyDoom(mutablePlayers, playerId);
          nextTurnPlayerId = advanceTurn(mutablePlayers, playerId);
        }
        case SNACK_STASH -> {
          // Effect is NOT applied automatically — client must confirm.
          snackStashPending = true;
          pendingPlayerId = playerId;
          // Turn does not advance until confirmation is received.
        }
        case NORMAL -> nextTurnPlayerId = advanceTurn(mutablePlayers, playerId);
      }

      GameState updated =
        new GameState(gameId, mutablePlayers, nextTurnPlayerId, snackStashPending, pendingPlayerId);
      activeGames.put(gameId, updated);
      return updated;
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    /**
     * Throws {@link IllegalArgumentException} when {@code playerId} is not the active player.
     *
     * @param state    current game state
     * @param playerId the player attempting an action
     */
    private static void validateTurn(GameState state, String playerId) {
      if (!playerId.equals(state.getCurrentTurnPlayerId())) {
        throw new IllegalArgumentException(
          String.format("Not player %s's turn. Current turn: %s", playerId, state.getCurrentTurnPlayerId()));
      }
    }

    /**
     * Simulates drawing one card at random.
     *
     * <p>Replace this with a proper per-session shuffled deck.
     */
    private Card drawCard() {
      Card[] values = Card.values();
      return values[random.nextInt(values.length)];
    }

    /**
     * Decrements the lives of {@code playerId} and eliminates them if lives reach zero.
     *
     * @param players  mutable player list (mutated in place)
     * @param playerId the player who drew DOOM
     */
    private static void applyDoom(List<Player> players, String playerId) {
      players.stream()
        .filter(p -> p.getId().equals(playerId))
        .findFirst()
        .ifPresent(Player::decrementLives);
    }

    /**
     * Returns the ID of the next non-eliminated player after {@code currentPlayerId}.
     *
     * @param players         full player list
     * @param currentPlayerId the player whose turn just ended
     * @return the next active player's ID, or {@code currentPlayerId} if no others remain
     */
    private static String advanceTurn(List<Player> players, String currentPlayerId) {
      List<Player> active =
        players.stream().filter(p -> !p.isEliminated()).collect(Collectors.toList());
      if (active.size() <= 1) {
        return currentPlayerId;
      }
      int currentIndex =
        IntStream.range(0, active.size())
          .filter(i -> active.get(i).getId().equals(currentPlayerId))
          .findFirst()
          .orElse(0);
      return active.get((currentIndex + 1) % active.size()).getId();
    }
  }


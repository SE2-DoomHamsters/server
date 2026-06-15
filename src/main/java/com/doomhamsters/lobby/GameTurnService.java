package com.doomhamsters.lobby;

import com.doomhamsters.CardType;
import com.doomhamsters.GameState;
import com.doomhamsters.Player;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

/**
 * Service zur Verwaltung der aktiven Spielzüge und der Kern-Spiellogik.
 */
@Service
public class GameTurnService {

  /** Laufende Spielzustände, keyed by gameId. */
  private final Map<String, GameState> activeGames = new ConcurrentHashMap<>();

  /** Feingranulare Locks, um Deadlocks über den gesamten Service zu verhindern. */
  private final Map<String, Object> gameLocks = new ConcurrentHashMap<>();

  private final SecureRandom random = new SecureRandom();

  /**
   * Registriert eine neue Spielsitzung.
   *
   * @param gameId  eindeutiger Bezeichner des Spiels
   * @param players initiale Spielerliste
   */
  public void registerGame(String gameId, List<Player> players) {
    String firstTurn = players.isEmpty() ? "" : players.get(0).getId();
    activeGames.put(
        gameId,
      new GameState(gameId, players, firstTurn, false, null));
  }

  /**
   * Liefert den aktuellen {@link GameState} für das gegebene Spiel, oder empty wenn nicht gefunden.
   */
  public Optional<GameState> findGame(String gameId) {
    return Optional.ofNullable(activeGames.get(gameId));
  }

  /**
   * Verarbeitet eine Draw-Aktion für {@code playerId} in {@code gameId}.
   *
   * @param gameId   die Zielspielsitzung
   * @param playerId der Spieler der ziehen möchte
   * @return der aktualisierte {@link GameState} bereit zum Broadcast
   * @throws NoSuchElementException   wenn {@code gameId} nicht gefunden wird
   * @throws IllegalArgumentException wenn es nicht {@code playerId}'s Turn ist
   */
  public GameState processDraw(String gameId, String playerId) {
    // 1. Thread-Locking nur noch für dieses EINE spezifische Spiel (verhindert Deadlocks)
    Object gameLock = gameLocks.computeIfAbsent(gameId, k -> new Object());

    synchronized (gameLock) {
      GameState state = findGame(gameId)
          .orElseThrow(() -> new NoSuchElementException("Game not found: " + gameId));

      validateTurn(state, playerId);

      List<Player> mutablePlayers = new ArrayList<>(state.getPlayers());
      CardType drawnCard = drawCard();

      boolean snackStashPending = false;
      String pendingPlayerId = null;
      String nextTurnPlayerId = state.getCurrentTurnPlayerId();

      switch (drawnCard) {
        case DOOM -> {
          applyDoom(mutablePlayers, playerId);
          nextTurnPlayerId = advanceTurn(mutablePlayers, playerId);
        }
        case SNACK_STASH -> {
          snackStashPending = true;
          pendingPlayerId = playerId;
        }
        case NORMAL -> nextTurnPlayerId = advanceTurn(mutablePlayers, playerId);
        default -> {
          return null;
        }
      }

      GameState updated = new GameState(gameId, mutablePlayers, nextTurnPlayerId,
          snackStashPending, pendingPlayerId);
      activeGames.put(gameId, updated);
      return updated;
    }
  }

  private static void validateTurn(GameState state, String playerId) {
    if (!playerId.equals(state.getCurrentTurnPlayerId())) {
      throw new IllegalArgumentException(
        String.format("Not player %s's turn. Current turn: %s",
          playerId, state.getCurrentTurnPlayerId()));
    }
  }

  private CardType drawCard() {
    CardType[] values = CardType.values();
    return values[random.nextInt(values.length)];
  }

  private static void applyDoom(List<Player> players, String playerId) {
    players.stream()
      .filter(p -> p.getId().equals(playerId))
      .findFirst()
        .ifPresent(Player::decrementLives);
  }

  private static String advanceTurn(List<Player> players, String currentPlayerId) {
    List<Player> active = players.stream()
        .filter(p -> !p.isEliminated())
        .collect(Collectors.toList());
    if (active.size() <= 1) {
      return currentPlayerId;
    }
    int currentIndex = IntStream.range(0, active.size())
        .filter(i -> active.get(i).getId().equals(currentPlayerId))
        .findFirst()
        .orElse(0);
    return active.get((currentIndex + 1) % active.size()).getId();
  }
}

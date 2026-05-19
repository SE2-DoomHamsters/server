package com.doomhamsters.lobby;

import com.doomhamsters.CardType;
import com.doomhamsters.GameState;
import com.doomhamsters.Player;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

/**
 * Kombinierter Service: verwaltet Lobbys (inkl. QR-Code) und aktive Spielsitzungen.
 *
 * <p>Alle spielverändernden Methoden sind per gameId {@code synchronized}, um
 * Race-Conditions bei gleichzeitigen WebSocket-Nachrichten zu verhindern.
 */
@Service
public class LobbyService {

  // --- Stores ------------------------------------------------------------------

  /** Aktive Lobbys, bevor das Spiel gestartet wird. */
  private final Map<String, Lobby> activeLobbies = new ConcurrentHashMap<>();

  /**
   * Laufende Spielzustände, keyed by gameId.
   * In Produktion durch einen verteilten Store ersetzen.
   */
  public final Map<String, GameState> activeGames = new ConcurrentHashMap<>();

  /** Deck-Simulation — pro Session durch ein echtes gemischtes Deck ersetzen. */
  private final Random random = new Random();

  // ============================================================================
  // Lobby-Methoden  (unverändert aus LobbyService)
  // ============================================================================

  /** Erstellt eine neue Lobby. */
  public Lobby createLobby(String groupName, User creator) {
    String lobbyId = groupName.toUpperCase().trim().replaceAll("\\s+", "_");
    Lobby lobby = new Lobby(lobbyId);

    List<User> members = new ArrayList<>();
    members.add(creator);
    lobby.setMembers(members);

    lobby.setQrCodeBase64(generateQrCode(lobbyId));
    activeLobbies.put(lobbyId, lobby);
    return lobby;
  }

  /** Fügt einen User hinzu oder aktualisiert ihn. */
  public Optional<Lobby> joinOrUpdateLobby(String lobbyId, User user) {
    Lobby lobby = activeLobbies.get(lobbyId);
    if (lobby != null) {
      List<User> currentMembers = lobby.getMembers();
      currentMembers.removeIf(m -> m.getId().equals(user.getId()));
      currentMembers.add(user);
      lobby.setMembers(currentMembers);
      return Optional.of(lobby);
    }
    return Optional.empty();
  }

  /** Liefert eine Lobby per ID. */
  public Lobby getLobby(String lobbyId) {
    return activeLobbies.get(lobbyId);
  }

  // ============================================================================
  // Game-Methoden  (aus GameService übernommen, Signaturen unverändert)
  // ============================================================================

  /**
   * Liefert den aktuellen {@link GameState} für das gegebene Spiel, oder empty wenn nicht
   * gefunden.
   */
  public Optional<GameState> findGame(String gameId) {
    return Optional.ofNullable(activeGames.get(gameId));
  }

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
   * Verarbeitet eine Draw-Aktion für {@code playerId} in {@code gameId}.
   *
   * <p>Zuerst wird die Turn-Validierung durchgeführt; ist der anfragende Spieler nicht
   * der aktive Spieler, wird eine {@link IllegalArgumentException} geworfen und
   * <em>kein Zustand verändert</em>.
   *
   * <p><b>Karteneffekte:</b>
   * <ul>
   *   <li>{@link CardType#DOOM} — lives {@code -1}; Spieler wird eliminiert wenn lives {@code 0}.
   *   <li>{@link CardType#SNACK_STASH} — als pending markiert; Client muss via
   *       {@code /app/game/{gameId}/confirm-snack} bestätigen.
   *   <li>{@link CardType#NORMAL} — kein Effekt; Turn wird weitergegeben.
   * </ul>
   *
   * @param gameId   die Zielspielsitzung
   * @param playerId der Spieler der ziehen möchte
   * @return der aktualisierte {@link GameState} bereit zum Broadcast
   * @throws NoSuchElementException   wenn {@code gameId} nicht gefunden wird
   * @throws IllegalArgumentException wenn es nicht {@code playerId}'s Turn ist
   */
  public synchronized GameState processDraw(String gameId, String playerId) {
    GameState state =
        findGame(gameId)
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
        // Turn wird erst nach Bestätigung weitergegeben.
      }
      case NORMAL -> nextTurnPlayerId = advanceTurn(mutablePlayers, playerId);

      default -> {
        return null;
      }
    }

    GameState updated =
        new GameState(gameId, mutablePlayers, nextTurnPlayerId, snackStashPending, pendingPlayerId);
    activeGames.put(gameId, updated);
    return updated;
  }

  // ============================================================================
  // Private Hilfsmethoden
  // ============================================================================

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

  private String generateQrCode(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    try {
      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
      return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    } catch (WriterException | IOException e) {
      return null;
    }
  }
}

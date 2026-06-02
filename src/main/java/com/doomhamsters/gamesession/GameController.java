package com.doomhamsters.gamesession;

import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.CardRegistry;
import com.doomhamsters.gamesession.dto.GameStateDto;
import com.doomhamsters.gamesession.dto.GameStateMapper;
import com.doomhamsters.lobby.Lobby;
import com.doomhamsters.lobby.LobbyRealtimePublisher;
import com.doomhamsters.lobby.LobbyService;
import com.doomhamsters.lobby.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Endpunkte zur Steuerung des Spiel-Lebenszyklus.
 *
 * <p>Das Starten nutzt HTTP Request-Response. Danach wird die
 * Antwort auch via STOMP an die Lobby gesendet.
 */
@Tag(name = "Game", description = "Game lifecycle — Spiel aus Lobby starten")
@RestController
@RequestMapping("/api/game")
public class  GameController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

  private final GameSessionService gameSessionService;
  private final LobbyService lobbyService;
  private final SimpMessagingTemplate messagingTemplate;
  private final LobbyRealtimePublisher realtimePublisher;
  private final GameStateMapper gameStateMapper;
  private final CardRegistry cardRegistry;

  /**
   * Initialisiert den GameController mit seinen Abhängigkeiten.
   *
   * @param gameSessionService Der Service für Spielsitzungen
   * @param lobbyService Der Service für die Lobbys
   * @param messagingTemplate Das Template für STOMP-Nachrichten
   * @param realtimePublisher Der Publisher für Lobby/Spielstart-Events
   * @param gameStateMapper Der Mapper für den Spielzustand
   * @param cardRegistry Die Registry für Karten-Commands
   */
  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public GameController(
      GameSessionService gameSessionService,
      LobbyService lobbyService,
      SimpMessagingTemplate messagingTemplate,
      LobbyRealtimePublisher realtimePublisher,
      GameStateMapper gameStateMapper,
      CardRegistry cardRegistry) {
    this.gameSessionService = gameSessionService;
    this.lobbyService = lobbyService;
    this.messagingTemplate = messagingTemplate;
    this.realtimePublisher = realtimePublisher;
    this.gameStateMapper = gameStateMapper;
    this.cardRegistry = cardRegistry;
  }

  /**
   * Startet ein neues Spiel aus der angegebenen Lobby.
   *
   * <p>POST /api/game/start
   *
   * @param lobbyId Die ID der Ziel-Lobby
   * @param userId Die ID des anfragenden Lobby-Mitglieds
   * @return GameStartResponse mit neuer Spiel-ID, oder 404 bei Fehler
   */
  @Operation(summary = "Startet ein neues Spiel",
      description = "Erstellt GameSession und sendet ID via STOMP.")
  @ApiResponse(responseCode = "200", description = "Spiel erfolgreich gestartet",
      content = @Content(schema = @Schema(implementation = GameStartResponse.class)))
  @ApiResponse(responseCode = "404", description = "Lobby nicht gefunden")
  @PostMapping("/start")
  public ResponseEntity<GameStartResponse> startGame(
      @Parameter(description = "ID der Lobby") @RequestParam String lobbyId,
      @Parameter(description = "ID des anfragenden Mitglieds")
      @RequestParam(required = false) String userId) {

    AtomicReference<GameSession> createdSession = new AtomicReference<>();
    try {
      return lobbyService.startGame(
          lobbyId,
          userId,
          lobby -> {
            GameSession session = createStartedSession(lobby);
            createdSession.set(session);
            return session.getGameId();
          }).map(outcome -> {
            realtimePublisher.broadcastGameStart(outcome.lobby().getLobbyId(), outcome.gameId());
            realtimePublisher.broadcastLobbySnapshot(outcome.lobby());
            if (outcome.created() && createdSession.get() != null) {
              broadcastInitialGameState(createdSession.get());
            }
            return ResponseEntity.ok(new GameStartResponse(outcome.gameId()));
          }).orElseGet(() -> ResponseEntity.notFound().build());
    } catch (SecurityException e) {
      LOGGER.warn("start rejected: lobbyId={}, userId={}, reason=not_member", lobbyId, userId);
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (IllegalArgumentException | IllegalStateException e) {
      LOGGER.warn(
          "start rejected: lobbyId={}, userId={}, reason={}",
          lobbyId,
          userId,
          e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  private GameSession createStartedSession(Lobby lobby) {
    GameSession session = gameSessionService.createSession(lobby.getLobbyId());

    List<String> playerIds = lobby.getMembers().stream()
        .map(User::getId)
        .collect(Collectors.toList());
    List<String> playerNames = lobby.getMembers().stream()
        .map(user -> user.getUsername() == null || user.getUsername().isBlank()
            ? user.getId()
            : user.getUsername())
        .collect(Collectors.toList());

    List<Card> actionCards = createActionCards();
    Card snackStash = new Card("ss_proto", "Snack Stash", "snack_stash");
    List<Card> doomCards = createDoomCards(playerIds.size() - 1);

    session.getGame().setupWithPlayers(playerIds, playerNames, actionCards, snackStash, doomCards);
    addTestingCommandCardsToHands(session.getGame());
    session.setStatus(GameSession.GameStatus.RUNNING);

    logInitialGameState(session);
    gameSessionService.saveSession(session);

    return session;
  }

  private void broadcastInitialGameState(GameSession session) {
    GameStateDto initialState = gameStateMapper.toFilteredDto(session, null);
    messagingTemplate.convertAndSend("/topic/game-state/" + session.getGameId(), initialState);
  }

  private void logInitialGameState(GameSession session) {
    Game game = session.getGame();

    LOGGER.info(
        "initial game state: gameId={}, resolvingDoomPlayerId={}, "
            + "pendingDoomRequiresInsertion={}, pendingDoomCardId={}, currentPlayerId={}, "
            + "playerLives={}, deckTop={}",
        session.getGameId(),
        game.getResolvingDoomPlayerId(),
        game.isPendingDoomRequiresInsertion(),
        game.getPendingDoomCardId(),
        currentPlayerId(game),
        playerLives(game),
        deckTop(game));
  }

  private String currentPlayerId(Game game) {
    if (game.getBoard() == null || game.getBoard().getCurrentPlayer() == null) {
      return null;
    }
    return game.getBoard().getCurrentPlayer().getId();
  }

  private Map<String, Integer> playerLives(Game game) {
    return game.getPlayers().stream()
        .collect(Collectors.toMap(Player::getId, Player::getLives));
  }

  private String deckTop(Game game) {
    if (game.getDeck() == null || game.getDeck().getCards().isEmpty()) {
      return null;
    }
    Card topCard = game.getDeck().getCards().get(0);
    return topCard.getId() + ":" + topCard.getType();
  }

  private List<Card> createActionCards() {
    List<Card> cards = new ArrayList<>();

    // ── Defensive cards ──────────────────────────────────────────────
    // Snack Stash (4): already added separately as the board card; 4 deck copies
    for (int i = 0; i < 4; i++) {
      cards.add(new Card("ss_deck_" + i, "Snack Stash", "snack_stash", (g, p) -> {}));
    }
    // Sign of Fate (4): +1 life
    for (int i = 0; i < 4; i++) {
      cards.add(new Card("sof_" + i, "Sign of Fate", "sign_of_fate", (g, p) -> {}));
    }

    // ── Control cards ─────────────────────────────────────────────────
    // Power Nap (4): skip draw phase
    for (int i = 0; i < 4; i++) {
      cards.add(new Card("pn_" + i, "Power Nap", "power_nap", (g, p) -> {}));
    }
    // Hyper Mode (4): next player gets +1 turn
    for (int i = 0; i < 4; i++) {
      cards.add(new Card("hm_" + i, "Hyper Mode", "hyper_mode", (g, p) -> {}));
    }
    // Squeak! (4): cancel another player's action
    for (int i = 0; i < 4; i++) {
      cards.add(new Card("sq_" + i, "Squeak!", "squeak", (g, p) -> {}));
    }
    // Tunnel Chaos (4): shuffle the deck
    for (int i = 0; i < 4; i++) {
      cards.add(new Card("tc_" + i, "Tunnel Chaos", "tunnel_chaos", (g, p) -> {}));
    }

    // ── Info cards ────────────────────────────────────────────────────
    // Sniff Ahead (4): look at top 3 cards
    for (int i = 0; i < 4; i++) {
      cards.add(new Card("sa_" + i, "Sniff Ahead", "sniff_ahead", (g, p) -> {}));
    }
    // Quick Peek (4): look at top card
    for (int i = 0; i < 4; i++) {
      cards.add(new Card("qp_" + i, "Quick Peek", "quick_peek", (g, p) -> {}));
    }

    // ── Action cards ──────────────────────────────────────────────────
    // Tiny Thief (4): steal a random card from another player
    for (int i = 0; i < 4; i++) {
      cards.add(new Card("tt_" + i, "Tiny Thief", "tiny_thief", (g, p) -> {}));
    }
    // Beg for Snacks (4): request a specific card from a player
    for (int i = 0; i < 4; i++) {
      cards.add(new Card("bfs_" + i, "Beg for Snacks", "beg_for_snacks", (g, p) -> {}));
    }
    // Cage Swap (4): swap hand with next player
    for (int i = 0; i < 4; i++) {
      cards.add(new Card("cs_" + i, "Cage Swap", "cage_swap", (g, p) -> {}));
    }

    // ── Hamster combo cards (5 types × 4 copies = 20) ────────────────
    String[][] hamsterTypes = {
      {"fat",     "Fat Hamster"},
      {"ninja",   "Ninja Hamster"},
      {"sleepy",  "Sleepy Hamster"},
      {"gremlin", "Gremlin Hamster"},
      {"zombi",   "Zombi Hamster"}
    };
    for (String[] h : hamsterTypes) {
      for (int i = 0; i < 4; i++) {
        cards.add(new Card("hamster_" + h[0] + "_" + i,
            h[1],
            "hamster_" + h[0],   // e.g. "hamster_ninja" — enables combo detection
            (g, p) -> {}));
      }
    }

    return cards;
  }

  private void addTestingCommandCardsToHands(Game game) {
    for (Player player : game.getPlayers()) {
      for (Card card : cardRegistry.createTestingCardsForPlayer(player.getId())) {
        player.addToHand(card);
      }
    }
  }

  private List<Card> createDoomCards(int count) {
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      cards.add(new Card("doom_" + i, "Doom Hamster", "doom"));
    }
    return cards;
  }
}

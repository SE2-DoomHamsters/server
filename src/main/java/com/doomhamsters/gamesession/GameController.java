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
 * Controller zur Steuerung von Spiel-Ereignissen.
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

  private final GameSessionService gameSessionService;
  private final LobbyService lobbyService;
  private final SimpMessagingTemplate messagingTemplate;
  private final LobbyRealtimePublisher realtimePublisher;
  private final GameStateMapper gameStateMapper;
  private final CardRegistry cardRegistry;

  /**
   * Initializes the GameController.
   *
   * @param gameSessionService game session service
   * @param lobbyService lobby service
   * @param messagingTemplate STOMP messaging template
   * @param realtimePublisher lobby/game-start publisher
   * @param gameStateMapper game state mapper
   * @param cardRegistry card registry
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
   * Starts a new game from the given lobby.
   *
   * @param lobbyId lobby identifier
   * @param userId lobby member identifier
   * @return game start response
   */
  @PostMapping("/start")
  public ResponseEntity<GameStartResponse> startGame(
      @RequestParam String lobbyId,
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

    List<Card> actionCards = createDummyActionCards();
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

  private List<Card> createDummyActionCards() {
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < 40; i++) {
      cards.add(new Card("act_" + i, "Aktion " + i, "action"));
    }
    for (int i = 0; i < 4; i++) {
      cards.add(new Card("ss_deck_" + i, "Snack Stash", "snack_stash"));
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

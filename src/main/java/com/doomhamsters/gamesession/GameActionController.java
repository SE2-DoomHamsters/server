package com.doomhamsters.gamesession;

import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.ActivateCardCommandRequest;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandPlayedEventDto;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import com.doomhamsters.gamesession.cardcommands.CardCommandResultEventDto;
import com.doomhamsters.gamesession.cardcommands.CardDefinition;
import com.doomhamsters.gamesession.cardcommands.CardRegistry;
import com.doomhamsters.gamesession.dto.CardDto;
import com.doomhamsters.gamesession.dto.DoomDrawnEventDto;
import com.doomhamsters.gamesession.dto.ErrorCode;
import com.doomhamsters.gamesession.dto.ErrorEventDto;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Handles authoritative game actions sent over STOMP.
 */
@Controller
public class GameActionController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(GameActionController.class);

  private static final String PRIVATE_QUEUE_PREFIX = PRIVATE_QUEUE_PREFIX;

  private final GameSessionService gameSessionService;

  private final GameSessionBroadcaster gameSessionBroadcaster;

  private final SimpMessagingTemplate messagingTemplate;

  private final ObjectMapper objectMapper;

  private final CardRegistry cardRegistry;

  /**
   * Constructs the action controller.
   *
   * @param gameSessionService session service
   * @param gameSessionBroadcaster session broadcaster
   * @param messagingTemplate broker publisher
   * @param objectMapper JSON parser
   * @param cardRegistry card registry
   */
  @Autowired
  public GameActionController(
      GameSessionService gameSessionService,
      GameSessionBroadcaster gameSessionBroadcaster,
      SimpMessagingTemplate messagingTemplate,
      ObjectMapper objectMapper,
      CardRegistry cardRegistry) {

    this.gameSessionService = gameSessionService;
    this.gameSessionBroadcaster = gameSessionBroadcaster;
    this.messagingTemplate = messagingTemplate;
    this.objectMapper = objectMapper;
    this.cardRegistry = cardRegistry;
  }

  /**
   * Activates one authoritative card command.
   *
   * @param gameId destination game id
   * @param payload action payload
   */
  @MessageMapping("/game/{gameId}/card/activate")
  public void activateCard(
      @DestinationVariable String gameId,
      @Payload String payload) {

    ActivateCardCommandRequest request = readActivateCardRequest(payload);
    GameSession session = loadSession(gameId);
    Game game = session.getGame();

    assertCurrentPlayer(game, request.getPlayerId());
    assertNoPendingDoomInsertion(game);
    assertNoResolvingDoom(game);

    Player player = findPlayer(game, request.getPlayerId());
    Card handCard = findCardInHand(player, request.getCardId());
    assertCardTypeMatches(handCard, request.getCardType());

    CardDefinition definition =
        cardRegistry.get(
            request.getCommandId(),
            request.getCardType());

    // Snapshot state before mutations so Squick can restore it.
    // Squick itself is never recorded — it must not be undoable.
    if (definition.isUndoable()) {
      game.getBoard().recordLastAction(new Game(game), definition.commandId());
    }

    Card playedCard = player.removeFromHand(request.getCardId());
    game.getBoard().discardCard(playedCard);

    CardCommandResult result =
        definition.execute(
            new CardCommandContext(
                session,
                game,
                player,
            playedCard,
            request.getParameters()));

    publishCardPlayedEvent(session.getGameId(), request, player, playedCard, definition, result);
    publishCardResultIfNeeded(session.getGameId(), request, playedCard, definition, result);
    saveAndBroadcast(session, request.getPlayerId());
  }

  /**
   * Draws one card for the current player.
   *
   * @param gameId destination game id
   * @param payload action payload containing playerId
   */
  @MessageMapping("/game/{gameId}/draw")
  public void draw(
      @DestinationVariable String gameId,
      @Payload String payload) {

    String playerId = readPlayerId(payload);
    GameSession session = loadSession(gameId);
    Game game = session.getGame();

    assertCurrentPlayer(game, playerId);
    assertNoResolvingDoom(game);

    LOGGER.info(
        "before draw: gameId={}, currentPlayerId={}, turnCount={}",
        gameId,
        currentPlayerId(game),
        turnCount(game));

    Game.DrawResult drawResult = game.drawForCurrentPlayerWithResult();

    if (!drawResult.cardDrawn()) {
      throw new IllegalStateException(
          game.getState() != Game.State.RUNNING
              ? "The game is already over."
              : "The deck is empty; there is no card to draw.");
    }

    LOGGER.info(
        "after draw: currentPlayerId={}, turnCount={}, handSizes={}",
        currentPlayerId(game),
        turnCount(game),
        handSizes(game));

    sendPrivateDoomEventIfNeeded(session.getGameId(), playerId, drawResult);
    saveAndBroadcast(session, playerId);
  }

  /**
   * Accepts pending Doom, applies life loss, and clears public Doom-resolution state.
   *
   * @param gameId destination game id
   * @param payload action payload containing playerId
   */
  @MessageMapping("/game/{gameId}/doom/ack")
  public void ackDoom(
      @DestinationVariable String gameId,
      @Payload String payload) {

    String playerId = readPlayerId(payload);
    GameSession session = loadSession(gameId);
    Game game = session.getGame();

    assertPlayerExists(game, playerId);
    assertResolvingDoomPlayer(game, playerId);
    assertNoPendingDoomInsertion(game);
    assertNoPendingSnackStashClaim(game);
    game.acceptPendingDoomWithLifeLoss();
    saveAndBroadcast(session, playerId);
  }

  /**
   * Inserts a neutralized Doom card at the resolving player's requested deck depth.
   *
   * @param gameId destination game id
   * @param payload action payload containing playerId and position
   */
  @MessageMapping("/game/{gameId}/doom/insert")
  public void insertDoom(
      @DestinationVariable String gameId,
      @Payload String payload) {

    String playerId = readPlayerId(payload);
    GameSession session = loadSession(gameId);
    Game game = session.getGame();

    assertPlayerExists(game, playerId);
    assertResolvingDoomPlayer(game, playerId);
    assertPendingDoomInsertion(game);
    game.insertPendingDoom(readPosition(payload));
    saveAndBroadcast(session, playerId);
  }

  /**
   * Compatibility alias for older clients that still send doom/accept.
   *
   * @param gameId destination game id
   * @param payload action payload containing playerId
   */
  @MessageMapping("/game/{gameId}/doom/accept")
  public void acceptDoom(
      @DestinationVariable String gameId,
      @Payload String payload) {

    ackDoom(gameId, payload);
  }

  /**
   * Advances the game to the next active player.
   *
   * @param gameId destination game id
   */
  @MessageMapping("/game/{gameId}/nextTurn")
  public void nextTurn(
      @DestinationVariable String gameId) {

    GameSession session = loadSession(gameId);
    Game game = session.getGame();
    final String playerId = currentPlayerId(game);

    LOGGER.info(
        "before nextTurn: currentPlayerId={}, turnCount={}",
        currentPlayerId(game),
        turnCount(game));

    game.advanceTurn();

    LOGGER.info(
        "after nextTurn: currentPlayerId={}, turnCount={}",
        currentPlayerId(game),
        turnCount(game));

    saveAndBroadcast(session, playerId);
  }

  private GameSession loadSession(String gameId) {
    return gameSessionService
        .getSession(gameId)
        .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + gameId));
  }

  private void saveAndBroadcast(
      GameSession session,
      String requestingPlayerId) {

    gameSessionBroadcaster.saveAndBroadcast(session, requestingPlayerId);
  }

  private void sendPrivateDoomEventIfNeeded(
      String gameId,
      String playerId,
      Game.DrawResult drawResult) {

    if (drawResult == null || !drawResult.doomDrawn()) {
      return;
    }

    messagingTemplate.convertAndSend(
        PRIVATE_QUEUE_PREFIX + gameId + "/" + playerId,
        new DoomDrawnEventDto(cardRegistry.toCardDto(drawResult.getDrawnCard())));
  }

  private CardDto toCardDto(Card card) {
    return cardRegistry.toCardDto(card);
  }

  private ActivateCardCommandRequest readActivateCardRequest(String payload) {
    try {
      ActivateCardCommandRequest request =
          objectMapper.readValue(
              payload,
              ActivateCardCommandRequest.class);

      assertTextPresent(request.getPlayerId(), "playerId");
      assertTextPresent(request.getCardId(), "cardId");
      assertTextPresent(request.getCardType(), "cardType");
      assertTextPresent(request.getCommandId(), "commandId");

      return request;
    } catch (JacksonException exception) {
      throw new IllegalArgumentException("Action payload must be valid JSON.", exception);
    }
  }

  private void publishCardPlayedEvent(
      String gameId,
      ActivateCardCommandRequest request,
      Player player,
      Card playedCard,
      CardDefinition definition,
      CardCommandResult result) {

    CardCommandPlayedEventDto event = new CardCommandPlayedEventDto();
    event.setPlayerId(player.getId());
    event.setPlayerName(player.getName());
    event.setCommandId(request.getCommandId());
    event.setCard(cardRegistry.toCardDto(playedCard, definition));
    event.setMessage(result.getPublicMessage());

    messagingTemplate.convertAndSend(
        "/topic/game/" + gameId,
        event);
  }

  private void publishCardResultIfNeeded(
      String gameId,
      ActivateCardCommandRequest request,
      Card playedCard,
      CardDefinition definition,
      CardCommandResult result) {

    if (!result.hasPrivateResult()) {
      return;
    }

    CardCommandResultEventDto event = new CardCommandResultEventDto();
    event.setPlayerId(request.getPlayerId());
    event.setCommandId(request.getCommandId());
    event.setCard(cardRegistry.toCardDto(playedCard, definition));
    event.setRevealedCard(toCardDto(result.getRevealedCard()));
    event.setRevealedCards(
        result.getRevealedCards().isEmpty()
            ? null
            : result.getRevealedCards().stream().map(this::toCardDto).toList());
    event.setMessage(result.getPrivateMessage());

    messagingTemplate.convertAndSend(
        PRIVATE_QUEUE_PREFIX + gameId + "/" + request.getPlayerId(),
        event);
  }

  private void assertTextPresent(
      String value,
      String fieldName) {

    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Action payload must contain " + fieldName + ".");
    }
  }

  private String readPlayerId(String payload) {
    JsonNode root;

    try {
      root = objectMapper.readTree(payload);
    } catch (JacksonException exception) {
      throw new IllegalArgumentException("Action payload must be valid JSON.", exception);
    }

    JsonNode playerId = root.get("playerId");
    if (playerId == null || !playerId.isString() || playerId.stringValue().isBlank()) {
      throw new IllegalArgumentException("Action payload must contain playerId.");
    }

    return playerId.stringValue();
  }

  private int readPosition(String payload) {
    JsonNode root;

    try {
      root = objectMapper.readTree(payload);
    } catch (JacksonException exception) {
      throw new IllegalArgumentException("Action payload must be valid JSON.", exception);
    }

    JsonNode position = root.get("position");
    if (position == null || !position.isInt()) {
      throw new IllegalArgumentException("Action payload must contain integer position.");
    }

    return position.asInt();
  }

  private void assertCurrentPlayer(
      Game game,
      String playerId) {

    String currentPlayerId = currentPlayerId(game);
    if (!playerId.equals(currentPlayerId)) {
      throw new IllegalArgumentException("It is not player " + playerId + "'s turn.");
    }
  }

  private void assertPlayerExists(
      Game game,
      String playerId) {

    findPlayer(game, playerId);
  }

  private void assertResolvingDoomPlayer(
      Game game,
      String playerId) {

    String resolvingPlayerId = game.getResolvingDoomPlayerId();
    if (resolvingPlayerId == null) {
      throw new IllegalStateException("No doom resolution is pending.");
    }

    if (!playerId.equals(resolvingPlayerId)) {
      throw new IllegalArgumentException(
          "Player " + playerId + " is not resolving doom.");
    }
  }

  private void assertPendingDoomInsertion(Game game) {
    if (!game.isPendingDoomRequiresInsertion()) {
      throw new IllegalStateException("No pending Doom card requires insertion.");
    }
  }

  private void assertNoPendingDoomInsertion(Game game) {
    if (game.isPendingDoomRequiresInsertion()) {
      throw new IllegalStateException("Pending Doom insertion must be completed with doom/insert.");
    }
  }

  private void assertNoPendingSnackStashClaim(Game game) {
    if (game.getPendingSnackStashClaim() != null) {
      throw new IllegalStateException("Pending Snack Stash claim must resolve first.");
    }
  }

  private void assertNoResolvingDoom(Game game) {
    if (game.getResolvingDoomPlayerId() != null) {
      throw new IllegalStateException("Doom resolution must be acknowledged before playing cards.");
    }
  }

  private void assertCardTypeMatches(
      Card card,
      String requestedCardType) {

    if (!requestedCardType.equals(card.getType())) {
      throw new IllegalArgumentException(
          "Card " + card.getId() + " is type " + card.getType()
              + ", not " + requestedCardType + ".");
    }
  }

  private Player findPlayer(
      Game game,
      String playerId) {

    return game.getPlayers().stream()
        .filter(player -> player.getId().equals(playerId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
  }

  private Card findCardInHand(
      Player player,
      String cardId) {

    return player.getHand().stream()
        .filter(card -> card.getId().equals(cardId))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException(
                "No card with id " + cardId + " in " + player.getName() + "'s hand"));
  }

  private String currentPlayerId(Game game) {
    if (game.getBoard() == null
        || game.getBoard().getCurrentPlayer() == null) {

      return null;
    }

    return game.getBoard().getCurrentPlayer().getId();
  }

  private int turnCount(Game game) {
    if (game.getBoard() == null) {
      return 0;
    }

    return game.getBoard().getTurnCount();
  }

  private String handSizes(Game game) {
    return game.getPlayers().stream()
        .collect(Collectors.toMap(
            Player::getId,
            player -> player.getHand().size()))
        .toString();
  }

  /**
   * Reports a rejected game action back to the originating player.
   *
   * <p>Catches expected validation failures from this controller's message mappings, logs them at
   * WARN, and sends a private error event to {@code /queue/game/{gameId}/{playerId}/errors}.
   *
   * @param exception the validation failure
   * @param message the original STOMP message
   */
  @MessageExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public void handleInvalidAction(RuntimeException exception, Message<?> message) {
    SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
    String gameId = extractGameId(accessor.getDestination());
    String playerId = extractPlayerIdOrNull(message.getPayload());

    ErrorCode code = (exception instanceof IllegalStateException)
        ? ErrorCode.ILLEGAL_STATE
        : ErrorCode.INVALID_ACTION;

    LOGGER.warn(
        "rejected action: gameId={}, playerId={}, code={}, reason={}",
        gameId,
        playerId,
        code,
        exception.getMessage());

    if (gameId == null || playerId == null) {
      return;
    }

    messagingTemplate.convertAndSend(
        PRIVATE_QUEUE_PREFIX + gameId + "/" + playerId + "/errors",
        new ErrorEventDto(code, exception.getMessage(), gameId));
  }

  /**
   * Reports an unexpected server error to the originating player.
   *
   * <p>Catches anything not handled by {@link #handleInvalidAction}, logs it at ERROR with the
   * stack trace, and sends a generic error event without leaking internal details.
   *
   * @param exception the unexpected exception
   * @param message the original STOMP message
   */
  @MessageExceptionHandler(Exception.class)
  public void handleUnexpected(Exception exception, Message<?> message) {
    SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
    String gameId = extractGameId(accessor.getDestination());
    String playerId = extractPlayerIdOrNull(message.getPayload());

    LOGGER.error("unexpected action error: gameId={}, playerId={}", gameId, playerId, exception);

    if (gameId == null || playerId == null) {
      return;
    }

    messagingTemplate.convertAndSend(
        PRIVATE_QUEUE_PREFIX + gameId + "/" + playerId + "/errors",
        new ErrorEventDto(ErrorCode.INTERNAL_ERROR, "An unexpected error occurred.", gameId));
  }

  private String extractGameId(String destination) {
    if (destination == null) {
      return null;
    }

    String[] parts = destination.split("/");
    for (int index = 0; index < parts.length - 1; index++) {
      if ("game".equals(parts[index])) {
        return parts[index + 1];
      }
    }

    return null;
  }

  private String extractPlayerIdOrNull(Object payload) {
    String json;
    if (payload instanceof byte[] bytes) {
      json = new String(bytes, StandardCharsets.UTF_8);
    } else if (payload instanceof String text) {
      json = text;
    } else {
      LOGGER.warn(
          "Unsupported payload type for error routing: {}",
          payload == null ? "null" : payload.getClass().getName());
      return null;
    }

    try {
      JsonNode playerId = objectMapper.readTree(json).get("playerId");
      return (playerId != null && playerId.isTextual()) ? playerId.asText() : null;
    } catch (JacksonException exception) {
      return null;
    }
  }
}

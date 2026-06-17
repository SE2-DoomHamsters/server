package com.doomhamsters.gamesession.snackstash;

import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import com.doomhamsters.gamesession.GameSessionBroadcaster;
import com.doomhamsters.gamesession.GameSessionService;
import java.util.Optional;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Handles Snack Stash claim and vote actions sent over STOMP.
 */
@Controller
public class SnackStashWebSocketController {

  private final GameSessionService gameSessionService;
  private final GameSessionBroadcaster gameSessionBroadcaster;
  private final SimpMessagingTemplate messagingTemplate;
  private final ObjectMapper objectMapper;
  private final SnackStashClaimService snackStashClaimService;

  /**
   * Creates the Snack Stash WebSocket controller.
   *
   * @param gameSessionService session service
   * @param gameSessionBroadcaster session broadcaster
   * @param messagingTemplate broker publisher
   * @param objectMapper JSON parser
   * @param snackStashClaimService claim service
   */
  public SnackStashWebSocketController(
      GameSessionService gameSessionService,
      GameSessionBroadcaster gameSessionBroadcaster,
      SimpMessagingTemplate messagingTemplate,
      ObjectMapper objectMapper,
      SnackStashClaimService snackStashClaimService) {

    this.gameSessionService = gameSessionService;
    this.gameSessionBroadcaster = gameSessionBroadcaster;
    this.messagingTemplate = messagingTemplate;
    this.objectMapper = objectMapper;
    this.snackStashClaimService = snackStashClaimService;
  }

  /**
   * Claims a selected hand card as Snack Stash during Doom resolution.
   *
   * @param gameId destination game id
   * @param payload action payload containing playerId and cardId
   */
  @MessageMapping("/game/{gameId}/snack-stash/claim")
  public void claimSnackStash(
      @DestinationVariable String gameId,
      @Payload String payload) {

    String playerId = readTextField(payload, "playerId");
    String cardId = readTextField(payload, "cardId");
    GameSession session = loadSession(gameId);
    SnackStashClaim claim =
        snackStashClaimService.claimSnackStash(
            session,
            playerId,
            cardId,
            System.currentTimeMillis());

    Player player = findPlayer(session.getGame(), playerId);
    publishSnackStashClaimEvent(session.getGameId(), claim, player);
    gameSessionBroadcaster.saveAndBroadcast(session, playerId);
  }

  /**
   * Votes on another player's pending Snack Stash claim.
   *
   * @param gameId destination game id
   * @param payload action payload containing playerId, claimId, and vote
   */
  @MessageMapping("/game/{gameId}/snack-stash/vote")
  public void voteSnackStash(
      @DestinationVariable String gameId,
      @Payload String payload) {

    String playerId = readTextField(payload, "playerId");
    String claimId = readTextField(payload, "claimId");
    SnackStashVote vote = readSnackStashVote(payload);
    GameSession session = loadSession(gameId);
    Optional<SnackStashResolution> resolution =
        snackStashClaimService.vote(
            session,
            playerId,
            claimId,
            vote);

    resolution.ifPresentOrElse(
        resolved -> publishSnackStashResolutionEvent(session.getGameId(), resolved),
        () -> publishSnackStashClaimProgressEvent(session));
    gameSessionBroadcaster.saveAndBroadcast(session, playerId);
  }

  private GameSession loadSession(String gameId) {
    return gameSessionService
        .getSession(gameId)
        .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + gameId));
  }

  private void publishSnackStashClaimEvent(
      String gameId,
      SnackStashClaim claim,
      Player player) {

    SnackStashClaimEventDto event = new SnackStashClaimEventDto();
    event.setClaimId(claim.getClaimId());
    event.setPlayerId(player.getId());
    event.setPlayerName(player.getName());
    event.setVotesRequired(claim.getVotesRequired());
    event.setVotesReceived(claim.getVotesReceived());
    event.setVotedPlayerIds(claim.getVotesByPlayerId().keySet().stream().toList());
    event.setMessage(player.getName() + " claims Snack Stash.");

    messagingTemplate.convertAndSend(
        "/topic/game/" + gameId,
        event);
  }

  private void publishSnackStashClaimProgressEvent(GameSession session) {
    SnackStashClaim claim = session.getGame().getPendingSnackStashClaim();
    if (claim == null) {
      return;
    }

    Player player = findPlayer(session.getGame(), claim.getPlayerId());
    publishSnackStashClaimEvent(session.getGameId(), claim, player);
  }

  private void publishSnackStashResolutionEvent(
      String gameId,
      SnackStashResolution resolution) {

    messagingTemplate.convertAndSend(
        "/topic/game/" + gameId,
        new SnackStashResolutionEventDto(resolution));
  }

  private SnackStashVote readSnackStashVote(String payload) {
    String value = readTextField(payload, "vote").trim().toUpperCase();
    try {
      return SnackStashVote.valueOf(value);
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("Snack Stash vote must be YES or NO.", exception);
    }
  }

  private String readTextField(
      String payload,
      String fieldName) {

    JsonNode root;

    try {
      root = objectMapper.readTree(payload);
    } catch (JacksonException exception) {
      throw new IllegalArgumentException("Action payload must be valid JSON.", exception);
    }

    JsonNode value = root.get(fieldName);
    if (value == null || !value.isString() || value.stringValue().isBlank()) {
      throw new IllegalArgumentException("Action payload must contain " + fieldName + ".");
    }

    return value.stringValue();
  }

  private Player findPlayer(
      Game game,
      String playerId) {

    return game.getPlayers().stream()
        .filter(player -> player.getId().equals(playerId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
  }
}

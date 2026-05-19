package com.doomhamsters.lobby;

import com.doomhamsters.gamesession.GameStartResponse;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Publishes authoritative lobby and game-start snapshots over STOMP. */
@Component
public class LobbyRealtimePublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(LobbyRealtimePublisher.class);

  private final SimpMessagingTemplate messagingTemplate;
  private final LobbySubscriptionTracker lobbySubscriptionTracker;

  /**
   * Creates a realtime publisher.
   *
   * @param messagingTemplate STOMP messaging template
   * @param lobbySubscriptionTracker tracker used for fanout diagnostics
   */
  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public LobbyRealtimePublisher(
      SimpMessagingTemplate messagingTemplate,
      LobbySubscriptionTracker lobbySubscriptionTracker) {
    this.messagingTemplate = messagingTemplate;
    this.lobbySubscriptionTracker = lobbySubscriptionTracker;
  }

  /**
   * Broadcasts a full lobby snapshot to all subscribers.
   *
   * @param lobby lobby snapshot to broadcast
   */
  public void broadcastLobbySnapshot(Lobby lobby) {
    String topic = "/topic/lobby/" + lobby.getLobbyId();
    int memberCount = lobby.getMembers().size();
    int subscriberCount = lobbySubscriptionTracker.subscriberCount(topic);

    LOGGER.info(
        "broadcast fanout: topic={}, lobbyId={}, memberCount={}, gameStarted={}, "
            + "gameId={}, subscriberCount={}",
        topic,
        lobby.getLobbyId(),
        memberCount,
        lobby.isGameStarted(),
        lobby.getGameId(),
        subscriberCount);

    messagingTemplate.convertAndSend(topic, new Lobby(lobby));
  }

  /**
   * Broadcasts the shared started game ID for a lobby.
   *
   * @param lobbyId lobby id whose subscribers should receive the event
   * @param gameId started game id
   */
  public void broadcastGameStart(String lobbyId, String gameId) {
    String topic = "/topic/game/" + lobbyId;
    LOGGER.info("broadcast fanout: topic={}, lobbyId={}, gameId={}", topic, lobbyId, gameId);
    messagingTemplate.convertAndSend(topic, new GameStartResponse(gameId));
  }
}

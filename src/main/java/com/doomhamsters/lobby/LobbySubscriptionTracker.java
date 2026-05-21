package com.doomhamsters.lobby;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * Tracks active lobby topic subscriptions for diagnostics.
 */
@Component
public class LobbySubscriptionTracker {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(LobbySubscriptionTracker.class);

  private static final String LOBBY_TOPIC_PREFIX = "/topic/lobby/";

  private final ConcurrentMap<String, Set<String>> subscribersByDestination =
      new ConcurrentHashMap<>();

  /**
   * Creates an empty lobby subscription tracker.
   */
  public LobbySubscriptionTracker() {
    // Required by Spring for this stateful component; subscriptions are added by event listeners.
  }

  /**
   * Records STOMP subscriptions to lobby topics.
   *
   * @param event subscribe event
   */
  @EventListener
  public void onSubscribe(SessionSubscribeEvent event) {
    SimpMessageHeaderAccessor accessor =
        SimpMessageHeaderAccessor.wrap(event.getMessage());

    recordSubscription(
        accessor.getDestination(),
        accessor.getSessionId());
  }

  /**
   * Removes disconnected sessions from all tracked lobby topics.
   *
   * @param event disconnect event
   */
  @EventListener
  public void onDisconnect(SessionDisconnectEvent event) {
    recordDisconnect(event.getSessionId());
  }

  /**
   * Records a subscription for tests and event handlers.
   *
   * @param destination subscribed destination
   * @param sessionId STOMP session id
   */
  public void recordSubscription(
      String destination,
      String sessionId) {

    if (destination == null
        || sessionId == null
        || !destination.startsWith(LOBBY_TOPIC_PREFIX)) {

      return;
    }

    Set<String> subscribers =
        subscribersByDestination.computeIfAbsent(
            destination,
            ignored -> ConcurrentHashMap.newKeySet());
    subscribers.add(sessionId);

    LOGGER.info(
        "lobby subscription active: destination={}, sessionId={}, subscriberCount={}",
        destination,
        sessionId,
        subscribers.size());
  }

  /**
   * Removes a session from all tracked subscriptions.
   *
   * @param sessionId STOMP session id
   */
  public void recordDisconnect(String sessionId) {
    if (sessionId == null) {
      return;
    }

    subscribersByDestination.forEach((destination, subscribers) -> {
      if (subscribers.remove(sessionId)) {
        LOGGER.info(
            "lobby subscription removed: destination={}, sessionId={}, subscriberCount={}",
            destination,
            sessionId,
            subscribers.size());
      }
    });
  }

  /**
   * Counts sessions subscribed to a lobby topic.
   *
   * @param destination lobby topic destination
   * @return subscriber count
   */
  public int subscriberCount(String destination) {
    return subscribersByDestination
        .getOrDefault(destination, Collections.emptySet())
        .size();
  }

  /**
   * Returns the tracked session ids for a lobby topic.
   *
   * @param destination lobby topic destination
   * @return immutable session id snapshot
   */
  public Set<String> subscriberSessionIds(String destination) {
    return Set.copyOf(
        subscribersByDestination.getOrDefault(destination, Collections.emptySet()));
  }
}

package com.doomhamsters.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LobbySubscriptionTrackerTest {

  @Test
  void recordSubscriptionKeepsAllLobbySubscribers() {
    LobbySubscriptionTracker tracker = new LobbySubscriptionTracker();

    tracker.recordSubscription("/topic/lobby/ROOM_42", "host-session");
    tracker.recordSubscription("/topic/lobby/ROOM_42", "p2-session");
    tracker.recordSubscription("/topic/lobby/ROOM_42", "p3-session");

    assertEquals(3, tracker.subscriberCount("/topic/lobby/ROOM_42"));
  }

  @Test
  void recordDisconnectRemovesOnlyDisconnectedSession() {
    LobbySubscriptionTracker tracker = new LobbySubscriptionTracker();

    tracker.recordSubscription("/topic/lobby/ROOM_42", "host-session");
    tracker.recordSubscription("/topic/lobby/ROOM_42", "p2-session");
    tracker.recordSubscription("/topic/lobby/ROOM_42", "p3-session");

    tracker.recordDisconnect("p2-session");

    assertEquals(2, tracker.subscriberCount("/topic/lobby/ROOM_42"));
  }

  @Test
  void ignoresNonLobbySubscriptions() {
    LobbySubscriptionTracker tracker = new LobbySubscriptionTracker();

    tracker.recordSubscription("/topic/game/ROOM_42", "game-session");
    tracker.recordSubscription(null, "missing-destination");
    tracker.recordSubscription("/topic/lobby/ROOM_42", null);

    assertEquals(0, tracker.subscriberCount("/topic/lobby/ROOM_42"));
  }
}

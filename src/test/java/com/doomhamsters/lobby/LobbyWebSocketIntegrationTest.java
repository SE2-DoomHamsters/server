package com.doomhamsters.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LobbyWebSocketIntegrationTest {

  private static final String GROUP_NAME = "WebSocket Room";

  @LocalServerPort
  private int port;

  @Autowired
  private LobbySubscriptionTracker lobbySubscriptionTracker;

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final List<StompSession> sessions = new ArrayList<>();
  private final List<WebSocketStompClient> clients = new ArrayList<>();

  @AfterEach
  void disconnectClients() {
    sessions.stream()
        .filter(StompSession::isConnected)
        .forEach(StompSession::disconnect);
    clients.forEach(WebSocketStompClient::stop);
  }

  @Test
  void lobbyBroadcastReachesCreatorAndAllJoiners() throws Exception {
    LobbyPayload createdLobby = createLobby(new User("player-1", "Alice", "cat"));
    String lobbyId = createdLobby.lobbyId();
    String lobbyTopic = "/topic/lobby/" + lobbyId;

    BlockingQueue<LobbyPayload> hostMessages = subscribeClient(lobbyTopic);
    waitForSubscriberCount(lobbyTopic, 1);

    BlockingQueue<LobbyPayload> playerTwoMessages = subscribeClient(lobbyTopic);
    waitForSubscriberCount(lobbyTopic, 2);

    LobbyPayload playerTwoJoinResponse = joinLobby(lobbyId, new User("player-2", "Bob", "dog"));
    LobbyPayload hostTwoMemberBroadcast = waitForLobbyWithMemberCount(hostMessages, 2);
    LobbyPayload playerTwoBroadcast = waitForLobbyWithMemberCount(playerTwoMessages, 2);

    assertEquals(2, playerTwoJoinResponse.memberIds().size());
    assertSameMemberIds(playerTwoJoinResponse, hostTwoMemberBroadcast);
    assertSameMemberIds(playerTwoJoinResponse, playerTwoBroadcast);

    BlockingQueue<LobbyPayload> playerThreeMessages = subscribeClient(lobbyTopic);
    waitForSubscriberCount(lobbyTopic, 3);

    LobbyPayload playerThreeJoinResponse =
        joinLobby(lobbyId, new User("player-3", "Cara", "fox"));
    LobbyPayload hostThreeMemberBroadcast = waitForLobbyWithMemberCount(hostMessages, 3);
    LobbyPayload playerTwoThreeMemberBroadcast = waitForLobbyWithMemberCount(playerTwoMessages, 3);
    LobbyPayload playerThreeBroadcast = waitForLobbyWithMemberCount(playerThreeMessages, 3);

    assertEquals(3, playerThreeJoinResponse.memberIds().size());
    assertSameMemberIds(playerThreeJoinResponse, hostThreeMemberBroadcast);
    assertSameMemberIds(playerThreeJoinResponse, playerTwoThreeMemberBroadcast);
    assertSameMemberIds(playerThreeJoinResponse, playerThreeBroadcast);
    assertEquals(3, lobbySubscriptionTracker.subscriberCount(lobbyTopic));
  }

  private BlockingQueue<LobbyPayload> subscribeClient(String lobbyTopic) throws Exception {
    WebSocketStompClient client =
        new WebSocketStompClient(new StandardWebSocketClient());
    client.setMessageConverter(new RawStringMessageConverter());
    client.start();
    clients.add(client);

    StompSession session = client
        .connectAsync(webSocketUrl(), new StompSessionHandlerAdapter() {})
        .get(3, TimeUnit.SECONDS);
    sessions.add(session);

    BlockingQueue<LobbyPayload> messages = new LinkedBlockingQueue<>();
    session.subscribe(lobbyTopic, new LobbyFrameHandler(messages));
    return messages;
  }

  private LobbyPayload createLobby(User creator) throws Exception {
    String requestJson = """
        {
          "groupName": "%s",
          "user": %s
        }
        """.formatted(GROUP_NAME, userJson(creator));

    HttpResponse<String> response = postJson("/api/lobby/create", requestJson);
    assertEquals(200, response.statusCode());
    return parseLobby(response.body());
  }

  private LobbyPayload joinLobby(String lobbyId, User user) throws Exception {
    HttpResponse<String> response =
        postJson(
            "/api/lobby/" + lobbyId + "/join",
            userJson(user));

    assertEquals(200, response.statusCode());
    return parseLobby(response.body());
  }

  private HttpResponse<String> postJson(String path, String body) throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:" + port + path))
        .timeout(Duration.ofSeconds(3))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();

    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private String webSocketUrl() {
    return "ws://localhost:" + port + "/ws";
  }

  private String userJson(User user) {
    return """
        {
          "id": "%s",
          "username": "%s",
          "avatar": "%s"
        }
        """.formatted(user.getId(), user.getUsername(), user.getAvatar());
  }

  private LobbyPayload waitForLobbyWithMemberCount(
      BlockingQueue<LobbyPayload> messages,
      int expectedMemberCount) throws InterruptedException {

    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    LobbyPayload latest = null;

    while (System.nanoTime() < deadline) {
      LobbyPayload next = messages.poll(200, TimeUnit.MILLISECONDS);
      if (next != null) {
        latest = next;
        if (next.memberIds().size() == expectedMemberCount) {
          return next;
        }
      }
    }

    int latestMemberCount = latest == null ? 0 : latest.memberIds().size();
    throw new AssertionError(
        "Timed out waiting for lobby broadcast with "
            + expectedMemberCount
            + " members; latest member count was "
            + latestMemberCount);
  }

  private void waitForSubscriberCount(String lobbyTopic, int expectedSubscriberCount)
      throws InterruptedException {

    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    while (System.nanoTime() < deadline) {
      if (lobbySubscriptionTracker.subscriberCount(lobbyTopic)
          >= expectedSubscriberCount) {
        return;
      }
      TimeUnit.MILLISECONDS.sleep(100);
    }

    throw new AssertionError(
        "Timed out waiting for "
            + expectedSubscriberCount
            + " subscribers; tracked "
            + lobbySubscriptionTracker.subscriberCount(lobbyTopic));
  }

  private void assertSameMemberIds(LobbyPayload expected, LobbyPayload actual) {
    assertEquals(expected.memberIds(), actual.memberIds());
    assertEquals(expected.lobbyId(), actual.lobbyId());
    assertTrue(actual.hasQrCode());
    assertEquals(expected.gameIdIsNull(), actual.gameIdIsNull());
    assertEquals(expected.gameStarted(), actual.gameStarted());
  }

  private static LobbyPayload parseLobby(String json) {
    return new LobbyPayload(
        extractFirst(json, "\"lobbyId\"\\s*:\\s*\"([^\"]+)\""),
        extractAll(json, "\"id\"\\s*:\\s*\"([^\"]+)\""),
        json.contains("\"qrCodeBase64\":\""),
        json.contains("\"gameId\":null"),
        json.contains("\"gameStarted\":true"));
  }

  private static String extractFirst(String json, String pattern) {
    Matcher matcher = Pattern.compile(pattern).matcher(json);
    if (!matcher.find()) {
      throw new AssertionError("Could not extract pattern " + pattern + " from " + json);
    }
    return matcher.group(1);
  }

  private static List<String> extractAll(String json, String pattern) {
    Matcher matcher = Pattern.compile(pattern).matcher(json);
    List<String> values = new ArrayList<>();
    while (matcher.find()) {
      values.add(matcher.group(1));
    }
    return values;
  }

  private static final class LobbyFrameHandler implements StompFrameHandler {

    private final BlockingQueue<LobbyPayload> messages;

    private LobbyFrameHandler(BlockingQueue<LobbyPayload> messages) {
      this.messages = messages;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
      return String.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
      messages.add(parsePayload(payload));
    }

    private LobbyPayload parsePayload(Object payload) {
      String json = payload.toString();
      return parseLobby(json);
    }
  }

  private record LobbyPayload(
      String lobbyId,
      List<String> memberIds,
      boolean hasQrCode,
      boolean gameIdIsNull,
      boolean gameStarted) {}

  private static final class RawStringMessageConverter implements MessageConverter {

    @Override
    public Object fromMessage(Message<?> message, Class<?> targetClass) {
      Object payload = message.getPayload();
      if (payload instanceof byte[] bytes) {
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
      }
      return payload.toString();
    }

    @Override
    public Message<?> toMessage(Object payload, MessageHeaders headers) {
      return new GenericMessage<>(payload.toString(), headers);
    }
  }
}

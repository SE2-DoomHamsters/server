package com.doomhamsters.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Unit tests for LobbyController. */
class LobbyControllerTest {

  private LobbyService lobbyService;
  private LobbyRealtimePublisher realtimePublisher;
  private LobbyController controller;
  private User testUser;
  private Lobby testLobby;

  @BeforeEach
  void setUp() {
    lobbyService = mock(LobbyService.class);
    realtimePublisher = mock(LobbyRealtimePublisher.class);
    controller = new LobbyController(lobbyService, realtimePublisher);

    testUser = new User("u1", "HamsterPro", "hamster");
    testLobby = new Lobby("ROOM1234");
    testLobby.setGroupName("Test");
    testLobby.setHostId("u1");
    testLobby.setMembers(List.of(testUser));
    testLobby.setQrCodeBase64("base64qr==");
  }

  @Test
  void createLobbyReturnsCreatedAndBroadcastsSnapshot() {
    CreateLobbyRequest request = new CreateLobbyRequest();
    request.setGroupName("Test");
    request.setUser(testUser);
    when(lobbyService.createLobby("Test", testUser)).thenReturn(testLobby);

    ResponseEntity<?> response = controller.createLobby(request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    Lobby body = assertLobbyBody(response);
    assertEquals("ROOM1234", body.getLobbyId());
    assertEquals(1, body.getMembers().size());
    verify(realtimePublisher).broadcastLobbySnapshot(any(Lobby.class));
  }

  @Test
  void createLobbyReturnsBadRequestWhenServiceRejectsRequest() {
    CreateLobbyRequest request = new CreateLobbyRequest();
    request.setGroupName("Test");
    request.setUser(testUser);
    when(lobbyService.createLobby("Test", testUser))
        .thenThrow(new IllegalArgumentException("User id is required"));

    ResponseEntity<?> response = controller.createLobby(request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(Map.of("error", "User id is required"), response.getBody());
    verify(realtimePublisher, never()).broadcastLobbySnapshot(any(Lobby.class));
  }

  @Test
  void joinLobbyReturnsOkAndBroadcastsFullSnapshot() {
    User joiningUser = new User("u2", "Guest", "cat");
    Lobby updatedLobby = new Lobby("ROOM1234");
    updatedLobby.setMembers(List.of(testUser, joiningUser));
    when(lobbyService.joinOrUpdateLobby("ROOM1234", joiningUser))
        .thenReturn(Optional.of(updatedLobby));

    ResponseEntity<?> response = controller.joinLobby("ROOM1234", joiningUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    Lobby body = assertLobbyBody(response);
    assertEquals(2, body.getMembers().size());
    verify(realtimePublisher).broadcastLobbySnapshot(any(Lobby.class));
  }

  @Test
  void joinLobbyReturns404WhenLobbyNotFound() {
    when(lobbyService.joinOrUpdateLobby(anyString(), any())).thenReturn(Optional.empty());

    ResponseEntity<?> response = controller.joinLobby("missing", testUser);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(realtimePublisher, never()).broadcastLobbySnapshot(any(Lobby.class));
  }

  @Test
  void joinLobbyReturnsConflictWhenGameAlreadyStarted() {
    when(lobbyService.joinOrUpdateLobby("ROOM1234", testUser))
        .thenThrow(new IllegalStateException("Game already started"));

    ResponseEntity<?> response = controller.joinLobby("ROOM1234", testUser);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertEquals(Map.of("error", "Game already started"), response.getBody());
    verify(realtimePublisher, never()).broadcastLobbySnapshot(any(Lobby.class));
  }

  @Test
  void joinLobbyReturnsBadRequestWhenUserIsInvalid() {
    when(lobbyService.joinOrUpdateLobby("ROOM1234", testUser))
        .thenThrow(new IllegalArgumentException("User id is required"));

    ResponseEntity<?> response = controller.joinLobby("ROOM1234", testUser);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(Map.of("error", "User id is required"), response.getBody());
    verify(realtimePublisher, never()).broadcastLobbySnapshot(any(Lobby.class));
  }

  @Test
  void leaveLobbyBroadcastsUpdatedSnapshot() {
    Lobby updatedLobby = new Lobby("ROOM1234");
    updatedLobby.setMembers(List.of(new User("u2", "Guest", "cat")));
    when(lobbyService.leaveLobby("ROOM1234", "u1")).thenReturn(Optional.of(updatedLobby));
    LobbyController.PlayerIdRequest request = new LobbyController.PlayerIdRequest();
    request.setUserId("u1");

    ResponseEntity<Lobby> response = controller.leaveLobby("ROOM1234", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getMembers().size());
    verify(realtimePublisher).broadcastLobbySnapshot(any(Lobby.class));
  }

  @Test
  void leaveLobbyReturnsBadRequestWhenUserIdMissing() {
    LobbyController.PlayerIdRequest request = new LobbyController.PlayerIdRequest();

    ResponseEntity<Lobby> response = controller.leaveLobby("ROOM1234", request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    verify(realtimePublisher, never()).broadcastLobbySnapshot(any(Lobby.class));
  }

  @Test
  void leaveLobbyReturnsOkWithoutBodyWhenLobbyIsRemovedOrMissing() {
    when(lobbyService.leaveLobby("ROOM1234", "u1")).thenReturn(Optional.empty());
    LobbyController.PlayerIdRequest request = new LobbyController.PlayerIdRequest();
    request.setUserId("u1");

    ResponseEntity<Lobby> response = controller.leaveLobby("ROOM1234", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNull(response.getBody());
    verify(realtimePublisher, never()).broadcastLobbySnapshot(any(Lobby.class));
  }

  @Test
  void heartbeatReturnsSnapshotAndBroadcasts() {
    when(lobbyService.heartbeat("ROOM1234", "u1")).thenReturn(Optional.of(testLobby));
    LobbyController.PlayerIdRequest request = new LobbyController.PlayerIdRequest();
    request.setUserId("u1");

    ResponseEntity<Lobby> response = controller.heartbeat("ROOM1234", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("ROOM1234", response.getBody().getLobbyId());
    verify(realtimePublisher).broadcastLobbySnapshot(any(Lobby.class));
  }

  @Test
  void heartbeatReturnsBadRequestWhenUserIdMissing() {
    LobbyController.PlayerIdRequest request = new LobbyController.PlayerIdRequest();

    ResponseEntity<Lobby> response = controller.heartbeat("ROOM1234", request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    verify(realtimePublisher, never()).broadcastLobbySnapshot(any(Lobby.class));
  }

  @Test
  void heartbeatReturnsNotFoundWhenMemberOrLobbyMissing() {
    when(lobbyService.heartbeat("ROOM1234", "u1")).thenReturn(Optional.empty());
    LobbyController.PlayerIdRequest request = new LobbyController.PlayerIdRequest();
    request.setUserId("u1");

    ResponseEntity<Lobby> response = controller.heartbeat("ROOM1234", request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(realtimePublisher, never()).broadcastLobbySnapshot(any(Lobby.class));
  }

  @Test
  void getLobbyReturnsAuthoritativeSnapshot() {
    testLobby.setGameId("game-789");
    testLobby.setGameStarted(true);
    when(lobbyService.getLobby("ROOM1234")).thenReturn(testLobby);

    ResponseEntity<Lobby> response = controller.getLobby("ROOM1234");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("game-789", response.getBody().getGameId());
    assertTrue(response.getBody().isGameStarted());
  }

  @Test
  void getLobbyReturnsNotFoundWhenLobbyMissing() {
    when(lobbyService.getLobby("missing")).thenReturn(null);

    ResponseEntity<Lobby> response = controller.getLobby("missing");

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  private Lobby assertLobbyBody(ResponseEntity<?> response) {
    Object body = response.getBody();
    assertInstanceOf(Lobby.class, body);
    return (Lobby) body;
  }
}

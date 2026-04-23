package com.doomhamsters.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Unit tests for LobbyController.
 * Mocks LobbyService and SimpMessagingTemplate to isolate controller logic.
 */
class LobbyControllerTest {

  private LobbyService lobbyService;
  private SimpMessagingTemplate messagingTemplate;
  private LobbyController controller;

  private User testUser;
  private Lobby testLobby;

  @BeforeEach
  void setUp() {
    lobbyService = mock(LobbyService.class);
    messagingTemplate = mock(SimpMessagingTemplate.class);
    controller = new LobbyController(lobbyService, messagingTemplate);

    testUser = new User("u1", "HamsterPro", "🐹");
    testLobby = new Lobby("TEST");
    testLobby.setMembers(List.of(testUser));
    testLobby.setQrCodeBase64("base64qr==");
  }

  // ── createLobby ──────────────────────────────────────────────────────────

  @Test
  void createLobby_returnsOkWithLobby() {
    CreateLobbyRequest request = new CreateLobbyRequest();
    request.setGroupName("Test");
    request.setUser(testUser);
    when(lobbyService.createLobby("Test", testUser)).thenReturn(testLobby);

    ResponseEntity<Lobby> response = controller.createLobby(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("TEST", response.getBody().getLobbyId());
  }

  @Test
  void createLobby_returnsLobbyWithMembers() {
    CreateLobbyRequest request = new CreateLobbyRequest();
    request.setGroupName("Test");
    request.setUser(testUser);
    when(lobbyService.createLobby(any(), any())).thenReturn(testLobby);

    ResponseEntity<Lobby> response = controller.createLobby(request);

    assertEquals(1, response.getBody().getMembers().size());
    assertEquals("HamsterPro", response.getBody().getMembers().get(0).getUsername());
  }

  // ── joinLobby ─────────────────────────────────────────────────────────────

  @Test
  void joinLobby_returnsOkAndBroadcastsWhenLobbyExists() {
    User joiningUser = new User("u2", "Gast", "🐱");
    Lobby updatedLobby = new Lobby("TEST");
    updatedLobby.setMembers(List.of(testUser, joiningUser));
    when(lobbyService.joinOrUpdateLobby("TEST", joiningUser))
        .thenReturn(Optional.of(updatedLobby));

    ResponseEntity<Lobby> response = controller.joinLobby("TEST", joiningUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(2, response.getBody().getMembers().size());
    verify(messagingTemplate).convertAndSend(eq("/topic/lobby/TEST"), any(Lobby.class));
  }

  @Test
  void joinLobby_returns404WhenLobbyNotFound() {
    when(lobbyService.joinOrUpdateLobby(anyString(), any())).thenReturn(Optional.empty());

    ResponseEntity<Lobby> response = controller.joinLobby("DOES_NOT_EXIST", testUser);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
  }

  @Test
  void joinLobby_broadcastsToCorrectTopic() {
    User joiner = new User("u3", "NewPlayer", "🦊");
    Lobby updated = new Lobby("ROOM_42");
    updated.setMembers(List.of(joiner));
    when(lobbyService.joinOrUpdateLobby("ROOM_42", joiner)).thenReturn(Optional.of(updated));

    controller.joinLobby("ROOM_42", joiner);

    verify(messagingTemplate).convertAndSend("/topic/lobby/ROOM_42", updated);
  }

  // ── getLobby Status ──────────────────────────────────────────────────────────────

  @Test
  void getLobby_returnsOkWithLobbyWhenFound() {
    when(lobbyService.getLobby("TEST")).thenReturn(testLobby);

    ResponseEntity<Lobby> response = controller.getLobby("TEST");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("TEST", response.getBody().getLobbyId());
  }

  @Test
  void getLobby_returns404WhenNotFound() {
    when(lobbyService.getLobby("MISSING")).thenReturn(null);

    ResponseEntity<Lobby> response = controller.getLobby("MISSING");

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }
}

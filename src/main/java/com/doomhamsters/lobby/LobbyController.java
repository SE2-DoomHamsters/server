package com.doomhamsters.lobby;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for lobby management.
 *
 * <p>Create/join use HTTP request-response. After a join the updated lobby is
 * also broadcast via STOMP so all connected clients see the new member.
 */
@RestController
@RequestMapping("/api/lobby")
public class LobbyController {

  private final LobbyService lobbyService;
  private final SimpMessagingTemplate messagingTemplate;

  /** Constructs the controller with its dependencies. */
  public LobbyController(LobbyService lobbyService, SimpMessagingTemplate messagingTemplate) {
    this.lobbyService = lobbyService;
    this.messagingTemplate = messagingTemplate;
  }

  /**
   * Creates a new lobby and returns it.
   *
   * <p>POST /api/lobby/create
   *
   * @param request body containing {@code groupName} and the creator {@code user}
   * @return the created lobby with generated QR code
   */
  @PostMapping("/create")
  public ResponseEntity<Lobby> createLobby(@RequestBody CreateLobbyRequest request) {
    Lobby lobby = lobbyService.createLobby(request.getGroupName(), request.getUser());
    return ResponseEntity.ok(lobby);
  }

  /**
   * Adds a user to an existing lobby and broadcasts the updated lobby to all subscribers.
   *
   * <p>POST /api/lobby/{lobbyId}/join
   *
   * @param lobbyId the target lobby identifier
   * @param user the joining player
   * @return updated lobby, or 404 if the lobby does not exist
   */
  @PostMapping("/{lobbyId}/join")
  public ResponseEntity<Lobby> joinLobby(
      @PathVariable String lobbyId, @RequestBody User user) {
    return lobbyService
        .joinOrUpdateLobby(lobbyId, user)
        .map(
            lobby -> {
              // Broadcast to all clients subscribed to this lobby
              messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId, lobby);
              return ResponseEntity.ok(lobby);
            })
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * Returns the current state of a lobby.
   *
   * <p>GET /api/lobby/{lobbyId}
   *
   * @param lobbyId the target lobby identifier
   * @return lobby state, or 404 if not found
   */
  @GetMapping("/{lobbyId}")
  public ResponseEntity<Lobby> getLobby(@PathVariable String lobbyId) {
    Lobby lobby = lobbyService.getLobby(lobbyId);
    return lobby != null ? ResponseEntity.ok(lobby) : ResponseEntity.notFound().build();
  }
}

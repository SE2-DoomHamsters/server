package com.doomhamsters.lobby;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Lobby", description = "Lobby lifecycle — create, join and fetch lobby state")
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
  @Operation(summary = "Create a new lobby",
      description = "Creates a lobby with a QR code. The creator is added as the first member.")
  @ApiResponse(responseCode = "200", description = "Lobby created successfully",
      content = @Content(schema = @Schema(implementation = Lobby.class)))
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
  @Operation(summary = "Join an existing lobby",
      description = "Adds the player to the lobby and broadcasts the updated lobby"
          + " to all STOMP subscribers on /topic/lobby/{lobbyId}.")
  @ApiResponse(responseCode = "200", description = "Lobby joined successfully",
      content = @Content(schema = @Schema(implementation = Lobby.class)))
  @ApiResponse(responseCode = "404", description = "Lobby not found")
  @PostMapping("/{lobbyId}/join")
  public ResponseEntity<Lobby> joinLobby(
      @Parameter(description = "Lobby identifier") @PathVariable String lobbyId,
      @RequestBody User user) {
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
  @Operation(summary = "Get current lobby state")
  @ApiResponse(responseCode = "200", description = "Lobby found",
      content = @Content(schema = @Schema(implementation = Lobby.class)))
  @ApiResponse(responseCode = "404", description = "Lobby not found")
  @GetMapping("/{lobbyId}")
  public ResponseEntity<Lobby> getLobby(
      @Parameter(description = "Lobby identifier") @PathVariable String lobbyId) {
    Lobby lobby = lobbyService.getLobby(lobbyId);
    return lobby != null ? ResponseEntity.ok(lobby) : ResponseEntity.notFound().build();
  }
}

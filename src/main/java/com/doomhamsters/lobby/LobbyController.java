package com.doomhamsters.lobby;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  private static final String ERROR_KEY = "error";

  private final LobbyService lobbyService;
  private final LobbyRealtimePublisher realtimePublisher;

  /**
   * Constructs a new LobbyController.
   *
   * @param lobbyService lobby service
   * @param realtimePublisher realtime publisher
   */
  public LobbyController(
      LobbyService lobbyService,
      LobbyRealtimePublisher realtimePublisher) {
    this.lobbyService = lobbyService;
    this.realtimePublisher = realtimePublisher;
  }

  /**
   * Creates a new lobby.
   *
   * @param request create lobby request payload
   * @return the created lobby
   */
  @Operation(summary = "Create a new lobby",
      description = "Creates a lobby with a QR code. The creator is added as the first member.")
  @ApiResponse(responseCode = "201", description = "Lobby created successfully",
      content = @Content(schema = @Schema(implementation = Lobby.class)))
  @ApiResponse(responseCode = "400", description = "Invalid request")
  @PostMapping("/create")
  public ResponseEntity<Object> createLobby(@RequestBody CreateLobbyRequest request) {
    try {
      Lobby lobby = lobbyService.createLobby(request.getGroupName(), request.getUser());
      realtimePublisher.broadcastLobbySnapshot(lobby);
      return ResponseEntity.created(URI.create("/api/lobby/" + lobby.getLobbyId()))
          .body(new Lobby(lobby));
    } catch (IllegalArgumentException e) {
      return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Joins a lobby.
   *
   * @param lobbyId lobby identifier
   * @param user    user joining the lobby
   * @return updated lobby snapshot
   */
  @Operation(summary = "Join an existing lobby",
      description = "Adds the player to the lobby and broadcasts the updated lobby"
          + " to all STOMP subscribers on /topic/lobby/{lobbyId}.")
  @ApiResponse(responseCode = "200", description = "Lobby joined successfully",
      content = @Content(schema = @Schema(implementation = Lobby.class)))
  @ApiResponse(responseCode = "404", description = "Lobby not found")
  @ApiResponse(responseCode = "409", description = "Game already started")
  @PostMapping("/{lobbyId}/join")
  public ResponseEntity<Object> joinLobby(@PathVariable String lobbyId, @RequestBody User user) {
    try {
      return lobbyService.joinOrUpdateLobby(lobbyId, user).map(lobby -> {
        Lobby snapshot = new Lobby(lobby);
        realtimePublisher.broadcastLobbySnapshot(snapshot);
        return ResponseEntity.ok((Object) snapshot);
      }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    } catch (IllegalStateException e) {
      return errorResponse(HttpStatus.CONFLICT, e.getMessage());
    } catch (IllegalArgumentException e) {
      return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Removes a user from the lobby.
   *
   * @param lobbyId lobby identifier
   * @param request payload containing the user ID leaving
   * @return updated lobby snapshot
   */
  @Operation(summary = "Leave a lobby",
      description = "Removes the player from the lobby. " +
        "If the host leaves, a new host is assigned.")
  @ApiResponse(responseCode = "200", description = "Left lobby successfully")
  @ApiResponse(responseCode = "400", description = "Invalid request")
  @PostMapping("/{lobbyId}/leave")
  public ResponseEntity<Lobby> leaveLobby(
      @PathVariable String lobbyId,
      @RequestBody PlayerIdRequest request) {
    if (request.getUserId() == null) {
      return ResponseEntity.badRequest().build();
    }

    Lobby updated = lobbyService.leaveLobby(lobbyId, request.getUserId()).orElse(null);
    if (updated != null) {
      Lobby snapshot = new Lobby(updated);
      realtimePublisher.broadcastLobbySnapshot(snapshot);
      return ResponseEntity.ok(snapshot);
    }

    return ResponseEntity.ok().build();
  }

  /**
   * Records that a lobby member is still connected.
   *
   * @param lobbyId lobby identifier
   * @param request payload containing the user ID
   * @return current lobby snapshot
   */
  @Operation(summary = "Send heartbeat",
      description = "Marks the player as still connected. " +
        "Must be called regularly to avoid timeout.")
  @ApiResponse(responseCode = "200", description = "Heartbeat accepted",
      content = @Content(schema = @Schema(implementation = Lobby.class)))
  @ApiResponse(responseCode = "400", description = "Invalid request")
  @ApiResponse(responseCode = "404", description = "Lobby or user not found")
  @PostMapping("/{lobbyId}/heartbeat")
  public ResponseEntity<Lobby> heartbeat(
      @PathVariable String lobbyId,
      @RequestBody PlayerIdRequest request) {
    if (request.getUserId() == null) {
      return ResponseEntity.badRequest().build();
    }

    Lobby updated = lobbyService.heartbeat(lobbyId, request.getUserId()).orElse(null);
    if (updated == null) {
      return ResponseEntity.notFound().build();
    }

    Lobby snapshot = new Lobby(updated);
    realtimePublisher.broadcastLobbySnapshot(snapshot);
    return ResponseEntity.ok(snapshot);
  }

  /**
   * Gets the current state of a lobby.
   *
   * @param lobbyId lobby identifier
   * @return lobby snapshot
   */
  @Operation(summary = "Get current lobby state")
  @ApiResponse(responseCode = "200", description = "Lobby found",
      content = @Content(schema = @Schema(implementation = Lobby.class)))
  @ApiResponse(responseCode = "404", description = "Lobby not found")
  @GetMapping("/{lobbyId}")
  public ResponseEntity<Lobby> getLobby(
      @Parameter(description = "Lobby identifier") @PathVariable String lobbyId) {
    Lobby lobby = lobbyService.getLobby(lobbyId);
    return lobby != null ? ResponseEntity.ok(new Lobby(lobby)) : ResponseEntity.notFound().build();
  }

  private ResponseEntity<Object> errorResponse(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(Map.of(ERROR_KEY, message));
  }

  /**
   * Request DTO for member-scoped lobby operations.
   */
  @Getter
  @Setter
  public static class PlayerIdRequest {
    private String userId;
  }
}

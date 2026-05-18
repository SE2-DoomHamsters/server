package com.doomhamsters.lobby;

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
 */
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
  @GetMapping("/{lobbyId}")
  public ResponseEntity<Lobby> getLobby(@PathVariable String lobbyId) {
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

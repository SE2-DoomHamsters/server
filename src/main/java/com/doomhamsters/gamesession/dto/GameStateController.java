package com.doomhamsters.gamesession.dto;

import com.doomhamsters.gamesession.GameSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for game state recovery and reconnects.
 */
@Tag(name = "Game State",
    description = "Game state recovery for reconnecting players — "
        + "returns filtered state with own hand visible and other players' hands hidden")
@RestController
@RequestMapping("/api/game")
public class GameStateController {

  private final GameSessionService gameSessionService;

  private final GameStateMapper gameStateMapper;

  /**
   * Constructs the controller with dependencies.
   *
   * @param gameSessionService session service
   * @param gameStateMapper DTO mapper
   */
  public GameStateController(
      GameSessionService gameSessionService,
      GameStateMapper gameStateMapper) {

    this.gameSessionService = gameSessionService;
    this.gameStateMapper = gameStateMapper;
  }

  /**
   * Returns the current filtered game state for a reconnecting player.
   *
   * <p>Other players' hands are hidden.
   *
   * <p>GET /api/game/{gameId}/state?playerId=...
   *
   * @param gameId the target game session
   * @param playerId the reconnecting player
   * @return filtered game state
   */
  @Operation(
      summary = "Get filtered game state for a reconnecting player",
      description = "Returns the current game state with the requesting player's full hand visible."
          + "All other players' hands are hidden — only hand size is shown. "
          + "Returns 404 if the game does not exist or the playerId is not part of this game.")
  @ApiResponse(responseCode = "200", description = "Game state returned successfully",
      content = @Content(schema = @Schema(implementation = GameStateDto.class)))
  @ApiResponse(responseCode = "404",
      description = "Game not found or player is not a member of this game")
  @GetMapping("/{gameId}/state")
  public ResponseEntity<GameStateDto> getGameState(
      @Parameter(description = "Game session identifier") @PathVariable String gameId,
      @Parameter(description = "ID of the reconnecting player") @RequestParam String playerId) {

    return gameSessionService
        .getSession(gameId)
        .filter(session -> session.getGame() != null
            && session.getGame().getPlayers().stream()
                .anyMatch(p -> p.getId().equals(playerId)))
        .map(session -> {

          GameStateDto dto =
              gameStateMapper.toFilteredDto(
                  session,
                  playerId);

          return ResponseEntity.ok(dto);
        })
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}

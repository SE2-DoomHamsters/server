package com.doomhamsters.gamesession.dto;

import com.doomhamsters.gamesession.GameSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for game state recovery and reconnects.
 */
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
  @GetMapping("/{gameId}/state")
  public ResponseEntity<GameStateDto> getGameState(
      @PathVariable String gameId,
      @RequestParam String playerId) {

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
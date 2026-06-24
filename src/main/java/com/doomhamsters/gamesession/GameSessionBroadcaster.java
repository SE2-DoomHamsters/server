package com.doomhamsters.gamesession;

import com.doomhamsters.Game;
import com.doomhamsters.gamesession.dto.GameStateDto;
import com.doomhamsters.gamesession.dto.GameStateMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Persists a game session and broadcasts the requesting player's filtered state.
 */
@Service
public class GameSessionBroadcaster {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(GameSessionBroadcaster.class);

  private final GameSessionService gameSessionService;
  private final GameStateMapper gameStateMapper;
  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Creates a session broadcaster.
   *
   * @param gameSessionService session service
   * @param gameStateMapper game state mapper
   * @param messagingTemplate broker publisher
   */
  public GameSessionBroadcaster(
      GameSessionService gameSessionService,
      GameStateMapper gameStateMapper,
      SimpMessagingTemplate messagingTemplate) {

    this.gameSessionService = gameSessionService;
    this.gameStateMapper = gameStateMapper;
    this.messagingTemplate = messagingTemplate;
  }

  /**
   * Saves the session and publishes the filtered game state.
   *
   * @param session session to save
   * @param requestingPlayerId player receiving private state
   */
  public void saveAndBroadcast(
      GameSession session,
      String requestingPlayerId) {

    gameSessionService.saveSession(session);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
          "after session save: gameId={}, currentPlayerId={}, turnCount={}",
          session.getGameId(),
          currentPlayerId(session.getGame()),
          turnCount(session.getGame()));
    }

    GameStateDto dto =
        gameStateMapper.toFilteredDto(
            session,
            requestingPlayerId);

    messagingTemplate.convertAndSend(
        "/topic/game-state/" + session.getGameId(),
        dto);
  }

  private String currentPlayerId(Game game) {
    if (game.getBoard() == null
        || game.getBoard().getCurrentPlayer() == null) {

      return null;
    }

    return game.getBoard().getCurrentPlayer().getId();
  }

  private int turnCount(Game game) {
    if (game.getBoard() == null) {
      return 0;
    }

    return game.getBoard().getTurnCount();
  }
}

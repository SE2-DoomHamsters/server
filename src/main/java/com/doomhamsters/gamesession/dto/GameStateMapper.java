package com.doomhamsters.gamesession.dto;

import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Maps internal game sessions to filtered DTOs for clients.
 */
@Component
public class GameStateMapper {

  /**
   * Creates a filtered game state DTO for a specific player.
   *
   * <p>Other players' hands are hidden.
   *
   * @param session the current game session
   * @param requestingPlayerId the player requesting the state
   * @return filtered game state DTO
   */
  public GameStateDto toFilteredDto(
      GameSession session,
      String requestingPlayerId) {

    Game game = session.getGame();

    GameStateDto dto = new GameStateDto();

    dto.setGameId(session.getGameId());
    dto.setGameState(game.getState().name());

    if (game.getBoard() != null
        && game.getBoard().getCurrentPlayer() != null) {

      dto.setCurrentPlayerId(
          game.getBoard().getCurrentPlayer().getId());

      dto.setTurnCount(
          game.getBoard().getTurnCount());
    }

    List<PlayerStateDto> playerDtos = new ArrayList<>();

    for (Player player : game.getPlayers()) {

      PlayerStateDto playerDto = new PlayerStateDto();

      playerDto.setPlayerId(player.getId());
      playerDto.setPlayerName(player.getName());
      playerDto.setLives(player.getLives());
      playerDto.setAlive(player.isAlive());
      playerDto.setHandSize(player.getHand().size());

      boolean isRequestingPlayer =
          player.getId().equals(requestingPlayerId);

      if (isRequestingPlayer) {

        List<CardDto> handDtos = new ArrayList<>();

        for (Card card : player.getHand()) {

          CardDto cardDto = new CardDto();

          cardDto.setId(card.getId());
          cardDto.setName(card.getName());
          cardDto.setType(card.getType());

          handDtos.add(cardDto);
        }

        playerDto.setHand(handDtos);
      }

      playerDtos.add(playerDto);
    }

    dto.setPlayers(playerDtos);

    return dto;
  }
}

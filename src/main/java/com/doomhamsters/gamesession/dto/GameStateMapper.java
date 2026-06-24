package com.doomhamsters.gamesession.dto;

import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import com.doomhamsters.gamesession.cardcommands.CardRegistry;
import com.doomhamsters.gamesession.snackstash.SnackStashClaim;
import com.doomhamsters.gamesession.snackstash.SnackStashClaimEventDto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Maps internal game sessions to filtered DTOs for clients.
 */
@Component
public class GameStateMapper {

  private final CardRegistry cardRegistry;

  /**
   * Creates a mapper with the built-in card registry.
   */
  public GameStateMapper() {
    this(CardRegistry.defaultRegistry());
  }

  /**
   * Creates a mapper with the supplied card registry.
   *
   * @param cardRegistry card registry
   */
  @Autowired
  public GameStateMapper(CardRegistry cardRegistry) {
    this.cardRegistry = cardRegistry;
  }

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
    dto.setResolvingDoomPlayerId(game.getResolvingDoomPlayerId());
    dto.setPendingDoomRequiresInsertion(game.isPendingDoomRequiresInsertion());
    dto.setPendingDoomCardId(game.getPendingDoomCardId());
    dto.setPendingSnackStashClaim(mapPendingSnackStashClaim(game));
    dto.setRemainingDeckSize(
        game.getDeck() == null ? 0 : game.getDeck().size());

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

          handDtos.add(cardRegistry.toCardDto(card));
        }

        playerDto.setHand(handDtos);
      }

      playerDtos.add(playerDto);
    }

    dto.setPlayers(playerDtos);

    return dto;
  }

  private SnackStashClaimEventDto mapPendingSnackStashClaim(Game game) {
    SnackStashClaim claim = game.getPendingSnackStashClaim();
    if (claim == null) {
      return null;
    }

    Player claimant =
        game.getPlayers().stream()
            .filter(player -> player.getId().equals(claim.getPlayerId()))
            .findFirst()
            .orElse(null);

    String playerName = claimant == null ? claim.getPlayerId() : claimant.getName();

    SnackStashClaimEventDto dto = new SnackStashClaimEventDto();
    dto.setClaimId(claim.getClaimId());
    dto.setPlayerId(claim.getPlayerId());
    dto.setPlayerName(playerName);
    dto.setVotesRequired(claim.getVotesRequired());
    dto.setVotesReceived(claim.getVotesReceived());
    dto.setVotedPlayerIds(claim.getVotesByPlayerId().keySet().stream().toList());
    dto.setMessage(playerName + " claims Snack Stash.");
    return dto;
  }
}

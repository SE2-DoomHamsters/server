package com.doomhamsters.gamesession.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO representing the visible game state for a client.
 */
@Getter
@Setter
public class GameStateDto {

  private String gameId;

  private String gameState;

  private String currentPlayerId;

  private String resolvingDoomPlayerId;

  private boolean pendingDoomRequiresInsertion;

  private String pendingDoomCardId;

  private int remainingDeckSize;

  private int turnCount;

  private List<PlayerStateDto> players = new ArrayList<>();

  public GameStateDto() {
    // Required by Jackson for response body serialization.
  }

  public List<PlayerStateDto> getPlayers() {
    return new ArrayList<>(players);
  }

  /**
   * Sets the visible players in the game state.
   *
   * @param players the player DTO list
   */
  public void setPlayers(List<PlayerStateDto> players) {
    this.players = players == null
        ? new ArrayList<>()
        : new ArrayList<>(players);
  }
}

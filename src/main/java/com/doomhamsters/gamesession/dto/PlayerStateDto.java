package com.doomhamsters.gamesession.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO representing a player's visible state.
 */
@Getter
@Setter
public class PlayerStateDto {

  private String playerId;

  private String playerName;

  private int lives;

  private boolean alive;

  private int handSize;

  private List<CardDto> hand = new ArrayList<>();

  public PlayerStateDto() {
    // Required by Jackson for response body serialization.
  }

  public List<CardDto> getHand() {
    return new ArrayList<>(hand);
  }

  /**
   * Sets the visible hand cards for the player.
   *
   * @param hand the visible hand cards
   */
  public void setHand(List<CardDto> hand) {
    this.hand = hand == null
        ? new ArrayList<>()
        : new ArrayList<>(hand);
  }
}

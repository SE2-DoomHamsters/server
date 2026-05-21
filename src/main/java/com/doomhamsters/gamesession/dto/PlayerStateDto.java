package com.doomhamsters.gamesession.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO representing a player's visible state.
 */
@Schema(description = "Player state visible in a filtered game state response")
@Getter
@Setter
public class PlayerStateDto {

  @Schema(description = "Unique player identifier", example = "player-1")
  private String playerId;

  @Schema(description = "Player display name", example = "DoomSlayer")
  private String playerName;

  @Schema(description = "Remaining player lives", example = "3")
  private int lives;

  @Schema(description = "Whether the player can still take turns", example = "true")
  private boolean alive;

  @Schema(description = "Number of cards in this player's hand", example = "7")
  private int handSize;

  @Schema(description = "Visible hand cards; only populated for the requesting player")
  private List<CardDto> hand = new ArrayList<>();

  /**
   * Creates an empty player state DTO.
   */
  public PlayerStateDto() {
    // Required by Jackson for response body serialization.
  }

  /**
   * Returns the visible hand cards.
   *
   * @return defensive card DTO list
   */
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

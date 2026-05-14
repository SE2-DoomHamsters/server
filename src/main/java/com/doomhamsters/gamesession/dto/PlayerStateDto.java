package com.doomhamsters.gamesession.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing a player's visible state.
 */
public class PlayerStateDto {

  private String playerId;

  private String playerName;

  private int lives;

  private boolean alive;

  private int handSize;

  private List<CardDto> hand = new ArrayList<>();

  public PlayerStateDto() {
  }

  public String getPlayerId() {
    return playerId;
  }

  public void setPlayerId(String playerId) {
    this.playerId = playerId;
  }

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public int getLives() {
    return lives;
  }

  public void setLives(int lives) {
    this.lives = lives;
  }

  public boolean isAlive() {
    return alive;
  }

  public void setAlive(boolean alive) {
    this.alive = alive;
  }

  public int getHandSize() {
    return handSize;
  }

  public void setHandSize(int handSize) {
    this.handSize = handSize;
  }

  public List<CardDto> getHand() {
    return new ArrayList<>(hand);
  }

  public void setHand(List<CardDto> hand) {
    this.hand = hand == null
        ? new ArrayList<>()
        : new ArrayList<>(hand);
  }
}
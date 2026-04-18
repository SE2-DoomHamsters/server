package com.doomhamsters.api;

import com.doomhamsters.player.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerStateResponse {
  public String name, id;
  public int lives;
  public boolean eliminated, alive;

  public PlayerStateResponse(Player player){
    this.id = player.getId();
    this.name = player.getName();
    this.lives = player.getLives();
    this.eliminated = player.eliminated;
    this.alive = player.isAlive();
  }

  public static List<PlayerStateResponse> fromPlayers(List<Player> players){
    List<PlayerStateResponse> playersResponses = new ArrayList<>();
    for(Player player : players){
      playersResponses.add(new PlayerStateResponse(player));
    }
    return playersResponses;
  }
}

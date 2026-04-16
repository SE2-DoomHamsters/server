package com.doomhamsters.api;

import com.doomhamsters.game.GameState;
import com.doomhamsters.player.Player;
import java.util.List;

public class GameStateResponse {

  private final List<PlayerStateResponse> players;
  private final PlayerStateResponse currentPlayer;
  private final GameState state;

  public GameStateResponse(List<Player> players, Player currentPlayer, GameState state) {
    this.players = PlayerStateResponse.fromPlayers(players);
    this.currentPlayer = new PlayerStateResponse(currentPlayer);
    this.state = state;
  }
}

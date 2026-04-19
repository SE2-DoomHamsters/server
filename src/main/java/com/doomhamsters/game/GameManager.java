package com.doomhamsters.game;

import com.doomhamsters.api.GameStateResponse;
import com.doomhamsters.player.Player;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class GameManager {

  private final Map<String, Game> games = new ConcurrentHashMap<>();

  public Game createGame(String hostName) {
    String id = UUID.randomUUID().toString();
    Game game = new Game(id);
    Player p = game.addPlayer(hostName);
    game.setHost(p);
    games.put(id, game);
    return game;
  }

  public Game getGame(String id) {
    return games.get(id);
  }

  public GameStateResponse getGameStateResponse(String id) {
    Game game = getGame(id);
    return new GameStateResponse(
        game.getPlayers(),
        game.getCurrentPlayer(),
        game.getCurrentState()
    );
  }

  public boolean startGame(String id, String token) {
    if (games.get(id).isHostToken(token)) {
      return false;
    }
    games.get(id).start();
    return true;
  }

  public void removeGame(String id) {
    games.remove(id);
  }

  public void kickPlayer(String id, String hostToken, String targetPlayerId) {
    Game game = getGame(id);
    if (game.isHostToken(hostToken)) {
      game.removePlayer(game.getPlayerById(targetPlayerId).getToken());
    }
  }

  public Player joinGame(String gameId, String playerName) {
    return getGame(gameId).addPlayer(playerName);
  }

  public void leaveGame(String gameId, String token) {
    getGame(gameId).removePlayer(token);
  }
}

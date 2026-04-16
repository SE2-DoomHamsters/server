package com.doomhamsters.game;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameManager {

  private final Map<String, Game> games = new ConcurrentHashMap<>();

  public Game createGame(String hostName) {
    String id = UUID.randomUUID().toString();
    Game game = new Game(id);
    game.addPlayer(hostName);
    games.put(id, game);
    return game;
  }

  public Game getGame(String id) {
    return games.get(id);
  }

  public void removeGame(String id) {
    games.remove(id);
  }
}

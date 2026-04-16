package com.doomhamsters.api;

import com.doomhamsters.api.dto.*;
import com.doomhamsters.card.CardType;
import com.doomhamsters.game.Game;
import com.doomhamsters.game.GameManager;
import com.doomhamsters.player.Deck;
import com.doomhamsters.player.Player;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameController {

  private final GameManager gameManager;

  public GameController(GameManager gameManager) {
    this.gameManager = gameManager;
  }

  @PostMapping
  public String createGame(@RequestBody CreateGameRequest req) {
    return gameManager.createGame(req.hostName).getId();
  }

  @PostMapping("/{id}/join")
  public Player joinGame(@PathVariable String id,
                         @RequestBody JoinGameRequest req) {
    return gameManager.getGame(id).addPlayer(req.playerName);
  }

  @PostMapping("/{id}/leave")
  public void leaveGame(@PathVariable String id,
                        @RequestBody LeaveGameRequest req) {
    gameManager.getGame(id).removePlayer(req.playerName);
  }

  @PostMapping("/{id}/start")
  public void startGame(@PathVariable String id,
                        @RequestBody StartGameRequest req) {
    gameManager.getGame(id).start();
  }

  @PostMapping("/{id}/play")
  public void playCard(@PathVariable String id,
                       @RequestBody PlayCardRequest req) {
    gameManager.getGame(id).playCard(req.playerName, req.cardType);
  }

  @PostMapping("/{id}/end-turn")
  public void endTurn(@PathVariable String id,
                      @RequestBody EndTurnRequest req) {
    gameManager.getGame(id).endTurn(req.playerName);
  }

  @PostMapping("/{id}/draw")
  public CardType drawCard(@PathVariable String id,
                           @RequestBody DrawCardRequest req) {
    return gameManager.getGame(id).drawCard(req.playerName);
  }

  //Get Endpoints
  @GetMapping("/{id}/players")
  public List<PlayerStateResponse> getPlayers(@PathVariable String id) {
    return PlayerStateResponse.fromPlayers(gameManager.getGame(id).getPlayers());
  }

  @GetMapping("/{id}/current-player")
  public PlayerStateResponse getCurrentPlayer(@PathVariable String id) {
    return new PlayerStateResponse(gameManager.getGame(id).getCurrentPlayer());
  }

  @GetMapping("/{id}/hand")
  public Deck getHand(@PathVariable String id,
                      @RequestParam String playerName) {
    return gameManager.getGame(id).getPlayers().stream().filter(p -> p.getName().equals(playerName)).findFirst().orElse(null).getDeck();
  }

  @GetMapping("/{id}/state")
  public GameStateResponse getState(@PathVariable String id) {
    Game game = gameManager.getGame(id);

    return new GameStateResponse(
      game.getPlayers(),
      game.getCurrentPlayer(),
      game.getCurrentState()
    );
  }
}

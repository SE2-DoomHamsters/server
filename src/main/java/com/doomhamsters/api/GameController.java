package com.doomhamsters.api;

import com.doomhamsters.api.dto.*;
import com.doomhamsters.card.Card;
import com.doomhamsters.game.GameManager;
import com.doomhamsters.player.Player;
import org.springframework.web.bind.annotation.*;

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
    gameManager.getGame(id).start(req.playerName);
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
  public Card drawCard(@PathVariable String id,
                       @RequestBody DrawCardRequest req) {
    return gameManager.getGame(id).drawCard(req.playerName);
  }

  @PostMapping("/{id}/kick")
  public void kickPlayer(@PathVariable String id,
                         @RequestBody KickPlayerRequest req) {
    gameManager.getGame(id).kickPlayer(req.hostName, req.targetPlayerName);
  }
}

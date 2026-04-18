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

  private final GameManager gm;

  public GameController(GameManager gameManager) {
    this.gm = gameManager;
  }
  private Game game(String id) {return gm.getGame(id);}

  @PostMapping
  public String createGame(@RequestBody CreateGameRequest req) {
    return gm.createGame(req.hostName).getId();
  }

  @PostMapping("/{id}/join")
  public Player joinGame(@PathVariable String id, @RequestBody JoinGameRequest req) {
    return gm.joinGame(id,req.playerName);
  }

  @PostMapping("/{id}/leave")
  public void leaveGame(@PathVariable String id, @RequestBody LeaveGameRequest req) {
    gm.leaveGame(id,req.playerId);
  }

  @PostMapping("/{id}/start")
  public boolean startGame(@PathVariable String id, @RequestBody StartGameRequest req) {
    return gm.startGame(id,req.playerId);
  }

  @PostMapping("/{id}/play")
  public void playCard(@PathVariable String id, @RequestBody PlayCardRequest req) {
    game(id).playCard(req.playerId, req.cardType);
  }

  @PostMapping("/{id}/end-turn")
  public void endTurn(@PathVariable String id, @RequestBody EndTurnRequest req) {
    game(id).endTurn(req.playerId);
  }

  @PostMapping("/{id}/draw")
  public CardType drawCard(@PathVariable String id, @RequestBody DrawCardRequest req) {
    return game(id).drawCard(req.playerId);
  }

  @PostMapping("/{id}/kick")
  public void kickPlayer(@PathVariable String id, @RequestBody KickPlayerRequest req) {
    gm.kickPlayer(id,req.hostId,req.targetPlayerId);
  }

  //Get Endpoints
  @GetMapping("/{id}/players")
  public List<PlayerStateResponse> getPlayers(@PathVariable String id) {
    return PlayerStateResponse.fromPlayers(game(id).getPlayers());
  }

  @GetMapping("/{id}/current-player")
  public PlayerStateResponse getCurrentPlayer(@PathVariable String id) {
    return new PlayerStateResponse(game(id).getCurrentPlayer());
  }

  @GetMapping("/{id}/hand")
  public Deck getHand(@PathVariable String id, @RequestParam String playerId) {
    return game(id).getPlayerById(playerId).getDeck();
  }

  @GetMapping("/{id}/state")
  public GameStateResponse getState(@PathVariable String id) {
    return gm.getGameStateResponse(id);
  }
}

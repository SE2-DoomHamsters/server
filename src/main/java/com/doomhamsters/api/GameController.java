package com.doomhamsters.api;

import com.doomhamsters.api.dto.CreateGameRequest;
import com.doomhamsters.api.dto.DrawCardRequest;
import com.doomhamsters.api.dto.EndTurnRequest;
import com.doomhamsters.api.dto.JoinGameRequest;
import com.doomhamsters.api.dto.KickPlayerRequest;
import com.doomhamsters.api.dto.LeaveGameRequest;
import com.doomhamsters.api.dto.PlayCardRequest;
import com.doomhamsters.api.dto.StartGameRequest;
import com.doomhamsters.card.CardType;
import com.doomhamsters.game.Game;
import com.doomhamsters.game.GameManager;
import com.doomhamsters.player.Deck;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games")
public class GameController {

  private final GameManager gm;

  public GameController(GameManager gameManager) {
    this.gm = gameManager;
  }

  private Game game(String id) {
    return gm.getGame(id);
  }

  @PostMapping
  public void createGame(@RequestBody CreateGameRequest req) {
    gm.createGame(req.hostName); //ToDo: add response
  }

  //ToDo: Add delete game request

  @PostMapping("/{id}/join")
  public void joinGame(@PathVariable String id, @RequestBody JoinGameRequest req) {
    gm.joinGame(id, req.playerName); //ToDo: add response
  }

  @PostMapping("/{id}/leave")
  public void leaveGame(@PathVariable String id, @RequestBody LeaveGameRequest req) {
    gm.leaveGame(id, req.token);
  }

  @PostMapping("/{id}/start")
  public boolean startGame(@PathVariable String id, @RequestBody StartGameRequest req) {
    return gm.startGame(id, req.token);
  }

  @PostMapping("/{id}/play")
  public void playCard(@PathVariable String id, @RequestBody PlayCardRequest req) {
    game(id).playCard(req.token, req.cardType);
  }

  @PostMapping("/{id}/end-turn")
  public void endTurn(@PathVariable String id, @RequestBody EndTurnRequest req) {
    game(id).endTurn(req.token);
  }

  @PostMapping("/{id}/draw")
  public CardType drawCard(@PathVariable String id, @RequestBody DrawCardRequest req) {
    return game(id).drawCard(req.token);
  }

  @PostMapping("/{id}/kick")
  public void kickPlayer(@PathVariable String id, @RequestBody KickPlayerRequest req) {
    gm.kickPlayer(id, req.token, req.targetPlayerId);
  }

  // Get Endpoints
  @GetMapping("/{id}/players")
  public List<PlayerStateResponse> getPlayers(@PathVariable String id) {
    return PlayerStateResponse.fromPlayers(game(id).getPlayers());
  }

  @GetMapping("/{id}/current-player")
  public PlayerStateResponse getCurrentPlayer(@PathVariable String id) {
    return new PlayerStateResponse(game(id).getCurrentPlayer());
  }

  @GetMapping("/{id}/hand")
  public Deck getHand(@PathVariable String id, @RequestParam String token) {
    return game(id).getPlayerByToken(token).getDeck();
  }

  @GetMapping("/{id}/state")
  public GameStateResponse getState(@PathVariable String id) {
    return gm.getGameStateResponse(id);
  }
}

package com.doomhamsters.gamesession;

import com.doomhamsters.Card;
import com.doomhamsters.lobby.Lobby;
import com.doomhamsters.lobby.LobbyService;
import com.doomhamsters.lobby.User;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller zur Steuerung von Spiel-Ereignissen, wie dem Starten eines Spiels aus einer Lobby.
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

  private final GameSessionService gameSessionService;
  private final LobbyService lobbyService;
  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Initialisiert den GameController.
   */
  public GameController(
    GameSessionService gameSessionService,
    LobbyService lobbyService,
    SimpMessagingTemplate messagingTemplate) {
    this.gameSessionService = gameSessionService;
    this.lobbyService = lobbyService;
    this.messagingTemplate = messagingTemplate;
  }

  /**
   * Startet ein neues Spiel aus der angegebenen Lobby.
   */
  @PostMapping("/start")
  public ResponseEntity<GameStartResponse> startGame(@RequestParam String lobbyId) {
    Lobby lobby = lobbyService.getLobby(lobbyId);
    if (lobby == null) {
      return ResponseEntity.notFound().build();
    }

    List<String> playerNames = lobby.getMembers().stream()
      .map(User::getUsername)
      .collect(Collectors.toList());

    // 1. Session erstellen
    GameSession session = gameSessionService.createSession(lobbyId);

    // 2. Setup durchführen
    List<Card> actionCards = createDummyActionCards();
    Card snackStash = new Card("ss_proto", "Snack Stash", "snack_stash");
    List<Card> doomCards = List.of(new Card("doom_1", "Doom Hamster", "doom"));

    session.getGame().setup(playerNames, actionCards, snackStash, doomCards);
    session.setStatus(GameSession.GameStatus.RUNNING);

    gameSessionService.saveSession(session);

    // 3. Antwort an den Host
    GameStartResponse response = new GameStartResponse(session.getGameId());

    messagingTemplate.convertAndSend("/topic/game/" + lobbyId, response);

    return ResponseEntity.ok(response);
  }

  private List<Card> createDummyActionCards() {
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < 35; i++) {
      cards.add(new Card("act_" + i, "Aktion " + i, "action"));
    }
    return cards;
  }
}

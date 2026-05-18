package com.doomhamsters.gamesession.dto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doomhamsters.Card;
import com.doomhamsters.gamesession.GameSession;
import com.doomhamsters.gamesession.GameSessionPersistenceService;
import com.doomhamsters.gamesession.GameSessionService;
import com.doomhamsters.gamesession.dto.GameStateMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class GameStateControllerTests {

  @TempDir
  File tempDir;

  private MockMvc mockMvc;

  private GameSessionService gameSessionService;

  @BeforeEach
  void setUp() {
    String filePath = new File(tempDir, "sessions.json").getAbsolutePath();
    gameSessionService =
        new GameSessionService(
            new GameSessionPersistenceService(filePath));

    GameStateMapper mapper =
        new GameStateMapper();

    GameStateController controller =
        new GameStateController(
            gameSessionService,
            mapper);

    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .build();
  }

  @Test
  void shouldReturn404WhenGameDoesNotExist() throws Exception {

    mockMvc.perform(
            get("/api/game/missing/state")
                .param("playerId", "p0"))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404WhenGameNotSetUp() throws Exception {

    GameSession session =
        gameSessionService.createSession("lobby-1");

    mockMvc.perform(
            get("/api/game/" + session.getGameId() + "/state")
                .param("playerId", "p0"))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404WhenPlayerNotInGame() throws Exception {

    GameSession session = gameSessionService.createSession("lobby-1");
    setupGame(session);

    mockMvc.perform(
            get("/api/game/" + session.getGameId() + "/state")
                .param("playerId", "unknown"))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn200WhenPlayerIsInGame() throws Exception {

    GameSession session = gameSessionService.createSession("lobby-1");
    setupGame(session);

    mockMvc.perform(
            get("/api/game/" + session.getGameId() + "/state")
                .param("playerId", "p0"))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturnInitialGameStateWithoutDoomResolution() throws Exception {

    GameSession session =
        gameSessionService.createSession("lobby-1");

    session.getGame().setup(
        List.of("Alice", "Bob"),
        actionCards(44),
        new Card("ss_proto", "Snack Stash", "snack_stash"),
        List.of(new Card("doom_0", "Doom Hamster", "doom")));
    gameSessionService.saveSession(session);

    mockMvc.perform(
            get("/api/game/" + session.getGameId() + "/state")
                .param("playerId", "p1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resolvingDoomPlayerId").doesNotExist())
        .andExpect(jsonPath("$.pendingDoomRequiresInsertion").value(false))
        .andExpect(jsonPath("$.pendingDoomCardId").doesNotExist())
        .andExpect(jsonPath("$.currentPlayerId").isNotEmpty())
        .andExpect(jsonPath("$.players[0].lives").value(3))
        .andExpect(jsonPath("$.players[1].lives").value(3));
  }

  private List<Card> actionCards(int count) {
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      cards.add(new Card("act_" + i, "Action " + i, "action"));
    }

    return cards;
  }

  private void setupGame(GameSession session) {
    Card snackStash = new Card("ss", "Snack Stash", "snack_stash", null);
    Card doom = new Card("doom-1", "Doom", "doom", null);
    List<Card> actionCards = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      actionCards.add(new Card("action-" + i, "Action " + i, "action", null));
    }
    session.getGame().setup(List.of("Alice", "Bob"), actionCards, snackStash, List.of(doom));
    gameSessionService.saveSession(session);
  }
}

package com.doomhamsters.gamesession.dto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doomhamsters.Card;
import com.doomhamsters.gamesession.GameSession;
import com.doomhamsters.gamesession.GameSessionPersistenceService;
import com.doomhamsters.gamesession.GameSessionService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class GameStateControllerTests {

  private MockMvc mockMvc;

  private GameSessionService gameSessionService;

  @BeforeEach
  void setUp() {

    gameSessionService =
        new GameSessionService(
            new GameSessionPersistenceService());

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
                .param("playerId", "p1"))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn200WhenGameExists() throws Exception {

    GameSession session =
        gameSessionService.createSession("lobby-1");

    mockMvc.perform(
            get("/api/game/" + session.getGameId() + "/state")
                .param("playerId", "p1"))
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
}

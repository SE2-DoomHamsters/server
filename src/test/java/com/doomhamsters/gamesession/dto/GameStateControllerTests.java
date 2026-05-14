package com.doomhamsters.gamesession.dto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.doomhamsters.gamesession.GameSession;
import com.doomhamsters.gamesession.GameSessionPersistenceService;
import com.doomhamsters.gamesession.GameSessionService;
import com.doomhamsters.gamesession.dto.GameStateMapper;
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
}
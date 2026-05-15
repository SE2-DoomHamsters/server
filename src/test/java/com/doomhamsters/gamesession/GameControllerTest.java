package com.doomhamsters.gamesession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.doomhamsters.lobby.Lobby;
import com.doomhamsters.lobby.LobbyService;
import com.doomhamsters.lobby.User;
import java.util.List;

@SpringBootTest
class GameControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @MockitoBean
  private GameSessionService gameSessionService;

  @MockitoBean
  private LobbyService lobbyService;

  @MockitoBean
  private SimpMessagingTemplate messagingTemplate;

  private Lobby testLobby;
  private GameSession testSession;

  @BeforeEach
  void setUp() {
    // Hier wird der MockMvc selbst zusammengebaut
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

    testLobby = new Lobby("TEST_LOBBY");
    testLobby.setMembers(List.of(
      new User("u1", "Player1", "cat"),
      new User("u2", "Player2", "dog")
    ));
    testSession = new GameSession("game-123", "TEST_LOBBY");
  }

  @Test
  void startGame_Success() throws Exception {
    when(lobbyService.getLobby("TEST_LOBBY")).thenReturn(testLobby);
    when(gameSessionService.createSession("TEST_LOBBY")).thenReturn(testSession);

    mockMvc.perform(post("/api/game/start")
        .param("lobbyId", "TEST_LOBBY"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.gameId").value("game-123"));

    verify(gameSessionService).saveSession(any(GameSession.class));
    verify(messagingTemplate).convertAndSend(eq("/topic/game/TEST_LOBBY"), any(GameStartResponse.class));
  }

  @Test
  void startGame_LobbyNotFound() throws Exception {
    when(lobbyService.getLobby("NON_EXISTENT")).thenReturn(null);

    mockMvc.perform(post("/api/game/start")
        .param("lobbyId", "NON_EXISTENT"))
      .andExpect(status().isNotFound());

    verify(gameSessionService, never()).createSession(anyString());
  }
}

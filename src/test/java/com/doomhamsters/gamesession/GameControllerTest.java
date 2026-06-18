package com.doomhamsters.gamesession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.doomhamsters.Card;
import com.doomhamsters.lobby.Lobby;
import com.doomhamsters.lobby.LobbyRealtimePublisher;
import com.doomhamsters.lobby.LobbyService;
import com.doomhamsters.lobby.User;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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

  @MockitoBean
  private LobbyRealtimePublisher realtimePublisher;

  private Lobby testLobby;
  private GameSession testSession;

  @BeforeEach
  void setUp() {
    // Hier wird der MockMvc selbst zusammengebaut
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

    testLobby = new Lobby("TEST_LOBBY");
    testLobby.setHostId("u1");
    testLobby.setMembers(List.of(
      new User("u1", "Player1", "cat"),
      new User("u2", "Player2", "dog")
    ));
    testSession = new GameSession("game-123", "TEST_LOBBY");
  }

  @Test
  void startGame_Success() throws Exception {
    stubNewStart("u1");

    mockMvc.perform(post("/api/game/start")
        .param("lobbyId", "TEST_LOBBY")
        .param("userId", "u1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.gameId").value("game-123"));

    verify(gameSessionService).saveSession(any(GameSession.class));
    verify(realtimePublisher).broadcastGameStart("TEST_LOBBY", "game-123");
    assertEquals(
        List.of("u1", "u2"),
        testSession.getGame().getPlayers().stream().map(player -> player.getId()).toList());
    assertEquals(
        List.of("Player1", "Player2"),
        testSession.getGame().getPlayers().stream().map(player -> player.getName()).toList());
  }

  @Test
  void startGame_PersistsAndBroadcastsStartedLobbyState() throws Exception {
    stubNewStart("u1");

    mockMvc.perform(post("/api/game/start")
        .param("lobbyId", "TEST_LOBBY")
        .param("userId", "u1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.gameId").value("game-123"));

    ArgumentCaptor<Lobby> lobbyCaptor = ArgumentCaptor.forClass(Lobby.class);
    verify(realtimePublisher).broadcastLobbySnapshot(lobbyCaptor.capture());

    Lobby broadcastLobby = lobbyCaptor.getValue();
    assertEquals("game-123", broadcastLobby.getGameId());
    assertEquals(true, broadcastLobby.isGameStarted());
    assertEquals(2, broadcastLobby.getMembers().size());
  }

  @Test
  void startGame_ReturnsExistingLobbyGameWithoutCreatingAnotherSession() throws Exception {
    testLobby.setGameId("game-existing");
    testLobby.setGameStarted(true);
    when(lobbyService.startGame(eq("TEST_LOBBY"), eq("u1"), any()))
        .thenReturn(Optional.of(new LobbyService.GameStartOutcome(
            startedLobby("game-existing"),
            "game-existing",
            false)));

    mockMvc.perform(post("/api/game/start")
        .param("lobbyId", "TEST_LOBBY")
        .param("userId", "u1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.gameId").value("game-existing"));

    verify(gameSessionService, never()).createSession(anyString());
    verify(gameSessionService, never()).saveSession(any(GameSession.class));
    verify(lobbyService, never()).markGameStarted(anyString(), anyString());
    ArgumentCaptor<Lobby> lobbyCaptor = ArgumentCaptor.forClass(Lobby.class);
    verify(realtimePublisher).broadcastLobbySnapshot(lobbyCaptor.capture());
    assertEquals("game-existing", lobbyCaptor.getValue().getGameId());
    assertEquals(true, lobbyCaptor.getValue().isGameStarted());
    verify(realtimePublisher).broadcastGameStart("TEST_LOBBY", "game-existing");
  }

  @Test
  void startGame_InsertsPlayerCountMinusOneDoomCards() throws Exception {
    testLobby.setMembers(List.of(
      new User("u1", "Player1", "cat"),
      new User("u2", "Player2", "dog"),
      new User("u3", "Player3", "fox")
    ));
    stubNewStart("u1");

    mockMvc.perform(post("/api/game/start")
        .param("lobbyId", "TEST_LOBBY")
        .param("userId", "u1"))
      .andExpect(status().isOk());

    long doomCount = testSession.getGame().getDeck().getCards().stream()
        .filter(Card::isDoom)
        .count();

    assertEquals(2, doomCount);
  }

  @Test
  void startGame_UsesConfiguredDeckSetupRules() throws Exception {
    testLobby.setMembers(List.of(
      new User("u1", "Player1", "cat"),
      new User("u2", "Player2", "dog"),
      new User("u3", "Player3", "fox")
    ));
    stubNewStart("u1");

    mockMvc.perform(post("/api/game/start")
        .param("lobbyId", "TEST_LOBBY")
        .param("userId", "u1"))
      .andExpect(status().isOk());

    long snackStashCardsInGame = testSession.getGame().getDeck().getCards().stream()
        .filter(Card::isSnackStash)
        .count()
        + testSession.getGame().getPlayers().stream()
            .flatMap(player -> player.getHand().stream())
            .filter(Card::isSnackStash)
            .count();

    assertEquals(31, testSession.getGame().getDeck().size());
    assertEquals(7, snackStashCardsInGame);
    testSession.getGame().getPlayers().forEach(player ->
        assertEquals(19, player.getHand().size()));
  }

  @Test
  void startGame_AddsImplementedCommandCardsToEachStartingHand() throws Exception {
    stubNewStart("u1");

    mockMvc.perform(post("/api/game/start")
        .param("lobbyId", "TEST_LOBBY")
        .param("userId", "u1"))
      .andExpect(status().isOk());

    testSession.getGame().getPlayers().forEach(player -> {
      long powerNapCount = player.getHand().stream()
          .filter(card -> "PowerNap".equals(card.getType()))
          .count();
      long quickPeekCount = player.getHand().stream()
          .filter(card -> "QuickPeek".equals(card.getType()))
          .count();
      long sniffAheadCount = player.getHand().stream()
          .filter(card -> "SniffAhead".equals(card.getType()))
          .count();
      long tunnelChaosCount = player.getHand().stream()
          .filter(card -> "TunnelChaos".equals(card.getType()))
          .count();
      long stablePowerNapIdCount = player.getHand().stream()
          .filter(card -> ("power_nap_" + player.getId()).equals(card.getId()))
          .filter(card -> "Power Nap".equals(card.getName()))
          .count();
      long stableQuickPeekIdCount = player.getHand().stream()
          .filter(card -> ("quick_peek_" + player.getId()).equals(card.getId()))
          .filter(card -> "Quick Peek".equals(card.getName()))
          .count();
      long stableSniffAheadIdCount = player.getHand().stream()
          .filter(card -> ("sniff_ahead_" + player.getId()).equals(card.getId()))
          .filter(card -> "Sniff Ahead".equals(card.getName()))
          .count();
      long stableTunnelChaosIdCount = player.getHand().stream()
          .filter(card -> ("tunnel_chaos_" + player.getId()).equals(card.getId()))
          .filter(card -> "Tunnel Chaos".equals(card.getName()))
          .count();

      assertEquals(1, powerNapCount);
      assertEquals(1, quickPeekCount);
      assertEquals(1, sniffAheadCount);
      assertEquals(1, tunnelChaosCount);
      assertEquals(1, stablePowerNapIdCount);
      assertEquals(1, stableQuickPeekIdCount);
      assertEquals(1, stableSniffAheadIdCount);
      assertEquals(1, stableTunnelChaosIdCount);
    });
  }

  @Test
  void startGame_DoesNotEnterDoomResolutionState() throws Exception {
    stubNewStart("u1");

    mockMvc.perform(post("/api/game/start")
        .param("lobbyId", "TEST_LOBBY")
        .param("userId", "u1"))
      .andExpect(status().isOk());

    assertNull(testSession.getGame().getResolvingDoomPlayerId());
    assertFalse(testSession.getGame().isPendingDoomRequiresInsertion());
    assertNull(testSession.getGame().getPendingDoomCardId());
    testSession.getGame().getPlayers().forEach(player ->
        assertEquals(3, player.getLives()));
  }

  @Test
  void startGame_LobbyNotFound() throws Exception {
    when(lobbyService.startGame(eq("NON_EXISTENT"), eq("u1"), any()))
        .thenReturn(Optional.empty());

    mockMvc.perform(post("/api/game/start")
        .param("lobbyId", "NON_EXISTENT")
        .param("userId", "u1"))
      .andExpect(status().isNotFound());

    verify(gameSessionService, never()).createSession(anyString());
  }

  @Test
  void startGame_NonMemberRejected() throws Exception {
    when(lobbyService.startGame(eq("TEST_LOBBY"), eq("u2"), any()))
        .thenThrow(new SecurityException("Only lobby members can start the game"));

    mockMvc.perform(post("/api/game/start")
        .param("lobbyId", "TEST_LOBBY")
        .param("userId", "u2"))
        .andExpect(status().isForbidden());
  }

  @Test
  void startGame_SecondPlayerCanStart() throws Exception {
    stubNewStart("u2");

    mockMvc.perform(post("/api/game/start")
        .param("lobbyId", "TEST_LOBBY")
        .param("userId", "u2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gameId").value("game-123"));

    verify(realtimePublisher).broadcastGameStart("TEST_LOBBY", "game-123");
  }

  private void stubNewStart(String userId) {
    when(gameSessionService.createSession("TEST_LOBBY")).thenReturn(testSession);
    when(lobbyService.startGame(eq("TEST_LOBBY"), eq(userId), any()))
        .thenAnswer(invocation -> {
          Function<Lobby, String> gameCreator = invocation.getArgument(2);
          String gameId = gameCreator.apply(new Lobby(testLobby));
          return Optional.of(new LobbyService.GameStartOutcome(
              startedLobby(gameId),
              gameId,
              true));
        });
  }

  private Lobby startedLobby(String gameId) {
    Lobby lobby = new Lobby("TEST_LOBBY");
    lobby.setMembers(testLobby.getMembers());
    lobby.setQrCodeBase64(testLobby.getQrCodeBase64());
    lobby.setGameId(gameId);
    lobby.setGameStarted(true);
    return lobby;
  }
}

package com.doomhamsters.gamesession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameSessionServiceTest {

  private GameSessionRepository mockRepository;
  private GameSessionPersistenceCoordinator mockCoordinator;
  private GameSessionService service;

  @BeforeEach
  void setUp() {
    mockRepository = mock(GameSessionRepository.class);
    mockCoordinator = mock(GameSessionPersistenceCoordinator.class);
    service = new GameSessionService(mockRepository, mockCoordinator);
  }

  @Test
  void testCreateSession() {
    String lobbyId = "lobby-1";
    GameSession session = service.createSession(lobbyId);

    assertNotNull(session);
    assertNotNull(session.getGameId());
    assertEquals(lobbyId, session.getLobbyId());

    verify(mockRepository, times(1)).store(any(GameSession.class));
    verify(mockCoordinator, times(1)).saveNow();
  }

  @Test
  void testGetSession() {
    GameSession mockSession = new GameSession("game-1", "lobby-x");
    when(mockRepository.findById("game-1")).thenReturn(Optional.of(mockSession));

    Optional<GameSession> retrieved = service.getSession("game-1");

    assertTrue(retrieved.isPresent());
    assertEquals("lobby-x", retrieved.get().getLobbyId());
    verify(mockRepository, times(1)).findById("game-1");
  }

  @Test
  void testSaveSession() {
    GameSession session = new GameSession("manual-id", "lobby-x");

    service.saveSession(session);

    verify(mockRepository, times(1)).store(eq(session));
    verify(mockCoordinator, times(1)).markDirty();
  }
}

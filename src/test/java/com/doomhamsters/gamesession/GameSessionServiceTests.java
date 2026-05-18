package com.doomhamsters.gamesession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameSessionServiceTests {

  private GameSessionService service;

  @BeforeEach
  void setUp() {
    service = new GameSessionService(new GameSessionPersistenceService());
  }

  @AfterEach
  void cleanup() throws IOException {
    Files.deleteIfExists(Path.of("sessions.json"));
  }

  @Test
  void testCreateSession() {
    String lobbyId = "lobby-1";
    GameSession session = service.createSession(lobbyId);

    assertNotNull(session);
    assertNotNull(session.getGameId());
    assertEquals(lobbyId, session.getLobbyId());

    // Prüfen, ob sie wirklich in der Map ist
    Optional<GameSession> retrieved = service.getSession(session.getGameId());
    assertTrue(retrieved.isPresent());
    assertEquals(session, retrieved.get());
  }

  @Test
  void testGetSessionNotFound() {
    Optional<GameSession> result = service.getSession("non-existent");
    assertTrue(result.isEmpty());
  }

  @Test
  void testSaveSession() {
    GameSession session = new GameSession("manual-id", "lobby-x");

    service.saveSession(session);

    Optional<GameSession> retrieved = service.getSession("manual-id");
    assertTrue(retrieved.isPresent());
    assertEquals("lobby-x", retrieved.get().getLobbyId());
  }

  @Test
  void testSessionsPersistAcrossServiceRestart() {

    GameSession created =
        service.createSession("persistent-lobby");

    GameSessionService restartedService =
        new GameSessionService(
            new GameSessionPersistenceService());

    Optional<GameSession> restored =
        restartedService.getSession(
            created.getGameId());

    assertTrue(restored.isPresent());

    assertEquals(
        "persistent-lobby",
        restored.get().getLobbyId());
  }

  @Test
  void flushDirtySessionsPersistsSavedSession() {
    GameSessionPersistenceService persistenceService = mock(GameSessionPersistenceService.class);
    when(persistenceService.loadSessions()).thenReturn(new ConcurrentHashMap<>());
    GameSessionService serviceWithMockPersistence = new GameSessionService(persistenceService);

    serviceWithMockPersistence.saveSession(new GameSession("manual-id", "lobby-x"));
    serviceWithMockPersistence.flushDirtySessions();
    serviceWithMockPersistence.flushDirtySessions();

    verify(persistenceService).saveSessions(any());
  }

  @Test
  void flushCleanSessionsDoesNotPersist() {
    GameSessionPersistenceService persistenceService = mock(GameSessionPersistenceService.class);
    when(persistenceService.loadSessions()).thenReturn(new ConcurrentHashMap<>());
    GameSessionService serviceWithMockPersistence = new GameSessionService(persistenceService);

    serviceWithMockPersistence.flushSessionsOnShutdown();

    verify(persistenceService, never()).saveSessions(any());
  }

  @Test
  void failedFlushKeepsSessionsDirtyForRetry() {
    GameSessionPersistenceService persistenceService = mock(GameSessionPersistenceService.class);
    when(persistenceService.loadSessions()).thenReturn(new ConcurrentHashMap<>());
    RuntimeException failure = new RuntimeException("disk unavailable");
    doThrow(failure).doNothing().when(persistenceService).saveSessions(any());
    GameSessionService serviceWithMockPersistence = new GameSessionService(persistenceService);
    serviceWithMockPersistence.saveSession(new GameSession("manual-id", "lobby-x"));

    RuntimeException thrown = assertThrows(
        RuntimeException.class,
        serviceWithMockPersistence::flushDirtySessions);

    assertEquals(failure, thrown);
    doNothing().when(persistenceService).saveSessions(any());
    serviceWithMockPersistence.flushSessionsOnShutdown();
    verify(persistenceService, times(2)).saveSessions(any());
  }
}

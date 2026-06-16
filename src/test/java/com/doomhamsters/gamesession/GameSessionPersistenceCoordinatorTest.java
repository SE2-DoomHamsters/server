package com.doomhamsters.gamesession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameSessionPersistenceCoordinatorTest {

  private GameSessionRepository mockRepository;
  private GameSessionPersistenceService mockPersistenceService;
  private GameSessionPersistenceCoordinator coordinator;

  @BeforeEach
  void setUp() {
    mockRepository = mock(GameSessionRepository.class);
    mockPersistenceService = mock(GameSessionPersistenceService.class);

    when(mockPersistenceService.loadSessions()).thenReturn(new ConcurrentHashMap<>());
    when(mockRepository.getAll()).thenReturn(new ConcurrentHashMap<>());

    coordinator = new GameSessionPersistenceCoordinator(mockRepository, mockPersistenceService);
  }

  @Test
  void testSaveNowForcesSave() {
    coordinator.saveNow();
    verify(mockPersistenceService, times(1)).saveSessions(any());
  }

  @Test
  void testFlushCleanSessionsDoesNotPersist() {
    coordinator.flushSessionsOnShutdown();
    verify(mockPersistenceService, never()).saveSessions(any());
  }

  @Test
  void testFlushDirtySessionsPersistsAndClearsFlag() {
    coordinator.markDirty();
    coordinator.flushDirtySessions();

    verify(mockPersistenceService, times(1)).saveSessions(any());

    coordinator.flushDirtySessions();
    verify(mockPersistenceService, times(1)).saveSessions(any());
  }

  @Test
  void testFailedFlushKeepsSessionsDirtyForRetry() {
    coordinator.markDirty();
    RuntimeException failure = new RuntimeException("Disk unavailable");

    doThrow(failure).doNothing().when(mockPersistenceService).saveSessions(any());

    RuntimeException thrown = assertThrows(
      RuntimeException.class,
      coordinator::flushDirtySessions
    );
    assertEquals(failure, thrown);

    doNothing().when(mockPersistenceService).saveSessions(any());

    coordinator.flushDirtySessions();
    verify(mockPersistenceService, times(2)).saveSessions(any());
  }
}

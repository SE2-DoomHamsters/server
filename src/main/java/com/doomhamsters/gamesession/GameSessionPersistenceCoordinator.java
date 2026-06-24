package com.doomhamsters.gamesession;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ConcurrentMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Coordinator for background synchronization of game sessions to the disk.
 */
@Component
public class GameSessionPersistenceCoordinator {

  private final GameSessionRepository repository;
  private final GameSessionPersistenceService persistenceService;
  private final Object monitor = new Object();
  private boolean dirty;

  /**
   * Initializes the coordinator and loads saved sessions.
   *
   * @param repository         the repository for data storage in RAM
   * @param persistenceService the handler for disk persistence
   */
  public GameSessionPersistenceCoordinator(
      GameSessionRepository repository,
      GameSessionPersistenceService persistenceService) {
    this.repository = repository;
    this.persistenceService = persistenceService;
    this.repository.loadAll(persistenceService.loadSessions());
  }

  /**
   * Marks the current memory state as dirty,
   * so it will be saved during the next flush.
   */
  public void markDirty() {
    synchronized (monitor) {
      dirty = true;
    }
  }

  /**
   * Forces an immediate save of the current state to the disk.
   */
  public void saveNow() {
    ConcurrentMap<String, GameSession> snapshot = repository.getAll();
    synchronized (monitor) {
      dirty = false;
    }
    executeSave(snapshot);
  }

  /**
   * Persists pending session changes to disk on a timer instead of blocking every game action.
   */
  @Scheduled(fixedDelayString = "${doomhamsters.game.persistence-interval-ms:2000}")
  public void flushDirtySessions() {
    saveIfDirty();
  }

  @PreDestroy
  public void flushSessionsOnShutdown() {
    saveIfDirty();
  }

  private void saveIfDirty() {
    ConcurrentMap<String, GameSession> snapshot;
    synchronized (monitor) {
      if (!dirty) {
        return;
      }
      dirty = false;
      snapshot = repository.getAll();
    }
    executeSave(snapshot);
  }

  private void executeSave(ConcurrentMap<String, GameSession> snapshot) {
    try {
      persistenceService.saveSessions(snapshot);
    } catch (RuntimeException error) {
      markDirty();
      throw error;
    }
  }
}

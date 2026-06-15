package com.doomhamsters.gamesession;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Koordinator für die Hintergrund-Synchronisation der Spielsitzungen auf die Festplatte.
 */
@Component
public class GameSessionPersistenceCoordinator {

  private final GameSessionRepository repository;
  private final GameSessionPersistenceService persistenceService;
  private final Object monitor = new Object();
  private boolean dirty;

  /**
   * Initialisiert den Koordinator und lädt gespeicherte Sitzungen.
   *
   * @param repository         Das Repository für die Datenhaltung im RAM
   * @param persistenceService Der Handler für die Festplatten-Persistenz
   */
  public GameSessionPersistenceCoordinator(
    GameSessionRepository repository,
    GameSessionPersistenceService persistenceService) {
    this.repository = repository;
    this.persistenceService = persistenceService;
    this.repository.loadAll(persistenceService.loadSessions());
  }

  /**
   * Markiert den aktuellen Speicherstand als schmutzig (dirty), sodass er beim nächsten Flush gespeichert wird.
   */
  public void markDirty() {
    synchronized (monitor) {
      dirty = true;
    }
  }

  /**
   * Erzwingt ein sofortiges Speichern des aktuellen Standes auf die Festplatte.
   */
  public void saveNow() {
    ConcurrentHashMap<String, GameSession> snapshot = repository.getAll();
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
    ConcurrentHashMap<String, GameSession> snapshot;
    synchronized (monitor) {
      if (!dirty) {
        return;
      }
      dirty = false;
      snapshot = repository.getAll();
    }
    executeSave(snapshot);
  }

  private void executeSave(ConcurrentHashMap<String, GameSession> snapshot) {
    try {
      persistenceService.saveSessions(snapshot);
    } catch (RuntimeException error) {
      markDirty();
      throw error;
    }
  }
}

package com.doomhamsters.gamesession;

import jakarta.annotation.PreDestroy;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for managing the lifecycle of game sessions.
 */
@Service
public class GameSessionService {

  private final ConcurrentHashMap<String, GameSession> sessions;

  private final GameSessionPersistenceService persistenceService;
  private final Object persistenceMonitor = new Object();
  private boolean persistenceDirty;

  /**
   * Constructs the service and restores persisted sessions.
   *
   * @param persistenceService persistence handler
   */
  public GameSessionService(GameSessionPersistenceService persistenceService) {

    this.persistenceService = persistenceService;

    this.sessions = new ConcurrentHashMap<>(persistenceService.loadSessions());
  }

  /**
   * Creates and stores a new game session for a given lobby.
   *
   * @param lobbyId the lobby to start the game from
   * @return the newly created session
   */
  public GameSession createSession(String lobbyId) {

    String gameId = UUID.randomUUID().toString();

    GameSession newSession = new GameSession(gameId, lobbyId);

    sessions.put(gameId, newSession);

    persistSessionsNow();

    return newSession;
  }

  /**
   * Retrieves an existing game session by its ID.
   *
   * @param gameId the ID of the session
   * @return an Optional containing the session if found
   */
  public Optional<GameSession> getSession(String gameId) {

    return Optional.ofNullable(sessions.get(gameId));
  }

  /**
   * Manually saves or updates a game session in the store.
   *
   * @param session the session to save
   */
  public void saveSession(GameSession session) {

    sessions.put(session.getGameId(), session);

    markPersistenceDirty();
  }

  /**
   * Persists pending session changes to disk on a timer instead of blocking every game action.
   */
  @Scheduled(fixedDelayString = "${doomhamsters.game.persistence-interval-ms:2000}")
  void flushDirtySessions() {
    persistSessionsIfDirty();
  }

  @PreDestroy
  void flushSessionsOnShutdown() {
    persistSessionsIfDirty();
  }

  private void markPersistenceDirty() {
    synchronized (persistenceMonitor) {
      persistenceDirty = true;
    }
  }

  private void persistSessionsNow() {
    ConcurrentHashMap<String, GameSession> snapshot = new ConcurrentHashMap<>(sessions);
    persistenceService.saveSessions(snapshot);
    synchronized (persistenceMonitor) {
      persistenceDirty = false;
    }
  }

  private void persistSessionsIfDirty() {
    ConcurrentHashMap<String, GameSession> snapshot;
    synchronized (persistenceMonitor) {
      if (!persistenceDirty) {
        return;
      }
      snapshot = new ConcurrentHashMap<>(sessions);
      persistenceDirty = false;
    }

    try {
      persistenceService.saveSessions(snapshot);
    } catch (RuntimeException error) {
      synchronized (persistenceMonitor) {
        persistenceDirty = true;
      }
      throw error;
    }
  }
}

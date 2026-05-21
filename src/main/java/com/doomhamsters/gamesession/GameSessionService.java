package com.doomhamsters.gamesession;

import jakarta.annotation.PreDestroy;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service zur Verwaltung des Lebenszyklus von Spielsitzungen.
 */
@Service
public class GameSessionService {

  private final ConcurrentHashMap<String, GameSession> sessions;

  private final GameSessionPersistenceService persistenceService;
  private final Object persistenceMonitor = new Object();
  private boolean persistenceDirty;

  /**
   * Initialisiert den Service und stellt gespeicherte Sitzungen wieder her.
   *
   * @param persistenceService Der Handler für die Persistenz
   */
  public GameSessionService(GameSessionPersistenceService persistenceService) {

    this.persistenceService = persistenceService;

    this.sessions = new ConcurrentHashMap<>(persistenceService.loadSessions());
  }

  /**
   * Erstellt und speichert eine neue Spielsitzung für eine gegebene Lobby.
   *
   * @param lobbyId Die Lobby, aus der das Spiel gestartet wird
   * @return Die neu erstellte Spielsitzung
   */
  public GameSession createSession(String lobbyId) {

    String gameId = UUID.randomUUID().toString();

    GameSession newSession = new GameSession(gameId, lobbyId);

    sessions.put(gameId, newSession);

    persistSessionsNow();

    return newSession;
  }

  /**
   * Ruft eine existierende Spielsitzung anhand ihrer ID ab.
   *
   * @param gameId Die ID der Sitzung
   * @return Ein Optional, das die Sitzung enthält, falls gefunden
   */
  public Optional<GameSession> getSession(String gameId) {

    return Optional.ofNullable(sessions.get(gameId));
  }

  /**
   * Speichert oder aktualisiert eine Spielsitzung manuell im Speicher.
   *
   * @param session Die zu speichernde Sitzung
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

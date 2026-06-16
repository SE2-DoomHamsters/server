package com.doomhamsters.gamesession;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * Repository zur in-memory Speicherung von Spielsitzungen.
 */
@Repository
public class GameSessionRepository {

  private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();

  /**
   * Speichert eine Spielsitzung im Speicher.
   *
   * @param session Die zu speichernde Sitzung
   */
  public void store(GameSession session) {
    sessions.put(session.getGameId(), session);
  }

  /**
   * Ruft eine existierende Spielsitzung anhand ihrer ID ab.
   *
   * @param gameId Die ID der Sitzung
   * @return Ein Optional, das die Sitzung enthält, falls gefunden
   */
  public Optional<GameSession> findById(String gameId) {
    return Optional.ofNullable(sessions.get(gameId));
  }

  /**
   * Gibt einen Snapshot aller aktuellen Spielsitzungen zurück.
   *
   * @return Eine Map aller Sitzungen
   */
  public ConcurrentHashMap<String, GameSession> getAll() {
    return new ConcurrentHashMap<>(sessions);
  }

  /**
   * Lädt initiale Sitzungen in den Speicher.
   *
   * @param loadedSessions Die zu ladenden Sitzungen
   */
  public void loadAll(Map<String, GameSession> loadedSessions) {
    sessions.putAll(loadedSessions);
  }
}

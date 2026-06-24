package com.doomhamsters.gamesession;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * Repository for in-memory storage of game sessions.
 */
@Repository
public class GameSessionRepository {

  private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();

  /**
   * Stores a game session in memory.
   *
   * @param session the session to store
   */
  public void store(GameSession session) {
    sessions.put(session.getGameId(), session);
  }

  /**
   * Retrieves an existing game session by its ID.
   *
   * @param gameId the ID of the session
   * @return an Optional containing the session if found
   */
  public Optional<GameSession> findById(String gameId) {
    return Optional.ofNullable(sessions.get(gameId));
  }

  /**
   * Returns a snapshot of all current game sessions.
   *
   * @return a map of all sessions
   */
  public ConcurrentHashMap<String, GameSession> getAll() {
    return new ConcurrentHashMap<>(sessions);
  }

  /**
   * Loads initial sessions into memory.
   *
   * @param loadedSessions the sessions to load
   */
  public void loadAll(Map<String, GameSession> loadedSessions) {
    sessions.putAll(loadedSessions);
  }
}

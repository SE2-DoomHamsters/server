package com.doomhamsters.gamesession;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Service for managing the lifecycle of game sessions.
 */
@Service
public class GameSessionService {

  private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();

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
  }
}

package com.doomhamsters.gamesession;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Service for managing the lifecycle of game sessions.
 */
@Service
public class GameSessionService {

  private final GameSessionRepository repository;
  private final GameSessionPersistenceCoordinator persistenceCoordinator;

  /**
   * Initializes the service with its dependencies.
   *
   * @param repository             the in-memory repository
   * @param persistenceCoordinator the persistence coordinator
   */
  public GameSessionService(
      GameSessionRepository repository,
      GameSessionPersistenceCoordinator persistenceCoordinator) {
    this.repository = repository;
    this.persistenceCoordinator = persistenceCoordinator;
  }

  /**
   * Creates and stores a new game session for a given lobby.
   *
   * @param lobbyId the ID of the lobby from which the game is started
   * @return the newly created game session
   */
  public GameSession createSession(String lobbyId) {
    String gameId = UUID.randomUUID().toString();
    GameSession newSession = new GameSession(gameId, lobbyId);

    repository.store(newSession);
    persistenceCoordinator.saveNow();

    return newSession;
  }

  /**
   * Retrieves an existing game session by its ID.
   *
   * @param gameId the ID of the session
   * @return an Optional containing the session if found
   */
  public Optional<GameSession> getSession(String gameId) {
    return repository.findById(gameId);
  }

  /**
   * Manually stores or updates a game session in memory.
   *
   * @param session the session to store
   */
  public void saveSession(GameSession session) {
    repository.store(session);
    persistenceCoordinator.markDirty();
  }
}

package com.doomhamsters.gamesession;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Service zur Verwaltung des Lebenszyklus von Spielsitzungen.
 */
@Service
public class GameSessionService {

  private final GameSessionRepository repository;
  private final GameSessionPersistenceCoordinator persistenceCoordinator;

  /**
   * Initialisiert den Service mit seinen Abhängigkeiten.
   *
   * @param repository             Das in-memory Repository
   * @param persistenceCoordinator Der Persistenz-Koordinator
   */
  public GameSessionService(
      GameSessionRepository repository,
      GameSessionPersistenceCoordinator persistenceCoordinator) {
    this.repository = repository;
    this.persistenceCoordinator = persistenceCoordinator;
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

    repository.store(newSession);
    persistenceCoordinator.saveNow();

    return newSession;
  }

  /**
   * Ruft eine existierende Spielsitzung anhand ihrer ID ab.
   *
   * @param gameId Die ID der Sitzung
   * @return Ein Optional, das die Sitzung enthält, falls gefunden
   */
  public Optional<GameSession> getSession(String gameId) {
    return repository.findById(gameId);
  }

  /**
   * Speichert oder aktualisiert eine Spielsitzung manuell im Speicher.
   *
   * @param session Die zu speichernde Sitzung
   */
  public void saveSession(GameSession session) {
    repository.store(session);
    persistenceCoordinator.markDirty();
  }
}

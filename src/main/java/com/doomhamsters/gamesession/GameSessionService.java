package com.doomhamsters.gamesession;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Service zur Verwaltung des Lebenszyklus von Spielsitzungen.
 */
@Service
public class GameSessionService {

  private final ConcurrentHashMap<String, GameSession> sessions;

  private final GameSessionPersistenceService persistenceService;

  /**
   * Initialisiert den Service und stellt gespeicherte Sitzungen wieder her.
   *
   * @param persistenceService Der Handler für die Persistenz
   */
  public GameSessionService(GameSessionPersistenceService persistenceService) {

    this.persistenceService = persistenceService;

    this.sessions = persistenceService.loadSessions();
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

    persistSessions();

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

    persistSessions();
  }

  /**
   * Speichert alle aktiven Sitzungen auf der Festplatte.
   */
  private void persistSessions() {

    persistenceService.saveSessions(sessions);
  }
}

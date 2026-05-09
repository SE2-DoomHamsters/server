package com.doomhamsters.gamesession;

import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionService {
  // Speichert alle aktiven Spielinstanzen
  private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();

  public GameSession createSession(String lobbyId) {
    String gameId = UUID.randomUUID().toString();
    GameSession newSession = new GameSession(gameId, lobbyId);
    sessions.put(gameId, newSession);
    return newSession;
  }

  public Optional<GameSession> getSession(String gameId) {
    return Optional.ofNullable(sessions.get(gameId));
  }
  public void saveSession(GameSession session) {
    sessions.put(session.getGameId(), session);
  }
}

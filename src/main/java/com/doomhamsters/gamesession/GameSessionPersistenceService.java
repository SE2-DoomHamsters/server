package com.doomhamsters.gamesession;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Handles persistence of game sessions to disk.
 */
@Service
public class GameSessionPersistenceService {

  private final String filePath;
  private final ObjectMapper objectMapper;

  public GameSessionPersistenceService(
      @Value("${sessions.file.path:sessions.json}") String filePath
  ) {
    this.filePath = filePath;
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Saves all sessions to disk.
   *
   * @param sessions all active sessions
   */
  public void saveSessions(ConcurrentHashMap<String, GameSession> sessions) {

    objectMapper.writerWithDefaultPrettyPrinter()
        .writeValue(new File(filePath), sessions);

  }

  /**
   * Loads sessions from disk.
   *
   * @return restored sessions or empty map if file does not exist
   */
  public ConcurrentHashMap<String, GameSession> loadSessions() {

    File file = new File(filePath);

    if (!file.exists()) {
      return new ConcurrentHashMap<>();
    }

    return objectMapper.readValue(
        file,
        new TypeReference<ConcurrentHashMap<String, GameSession>>() {});

  }
}

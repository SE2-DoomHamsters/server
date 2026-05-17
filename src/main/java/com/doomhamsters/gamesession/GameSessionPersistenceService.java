package com.doomhamsters.gamesession;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Handles persistence of game sessions to disk.
 */
@Service
public class GameSessionPersistenceService {

  private static final String FILE_PATH = "sessions.json";

  private final ObjectMapper objectMapper;

  public GameSessionPersistenceService() {
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Saves all sessions to disk.
   *
   * @param sessions all active sessions
   */
  public void saveSessions(ConcurrentHashMap<String, GameSession> sessions) {

    objectMapper.writerWithDefaultPrettyPrinter()
        .writeValue(new File(FILE_PATH), sessions);

  }

  /**
   * Loads sessions from disk.
   *
   * @return restored sessions or empty map if file does not exist
   */
  public ConcurrentHashMap<String, GameSession> loadSessions() {

    File file = new File(FILE_PATH);

    if (!file.exists()) {
      return new ConcurrentHashMap<>();
    }

    try {
      return objectMapper.readValue(
          file,
          new TypeReference<ConcurrentHashMap<String, GameSession>>() {});
    } catch (JacksonException exception) {
      return new ConcurrentHashMap<>();
    }

  }
}

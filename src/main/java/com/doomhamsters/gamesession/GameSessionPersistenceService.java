package com.doomhamsters.gamesession;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Handles persistence of game sessions to disk as JSON.
 *
 * <p>The file path is configurable via the {@code sessions.file.path} property
 * (default: {@code sessions.json} in the working directory).
 * In Docker, this is overridden by the {@code SESSIONS_FILE_PATH} environment variable
 * to {@code /data/sessions.json}, which is mounted to a named Docker volume so that
 * sessions survive container restarts and redeployments.
 *
 * <p>If saving or loading fails, the error is logged and the server continues running —
 * sessions are always available in memory even if disk persistence is unavailable.
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
    try {
      File file = new File(filePath);
      File parent = file.getParentFile();
      if (parent != null && !parent.exists() && !parent.mkdirs()) {
        System.err.println("Failed to create directory: " + parent.getAbsolutePath());
        return;
      }
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, sessions);
    } catch (Exception e) {
      System.err.println("Failed to save sessions: " + e.getMessage());
    }
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

    try {
      return objectMapper.readValue(
          file,
          new TypeReference<ConcurrentHashMap<String, GameSession>>() {});
    } catch (Exception e) {
      System.err.println("Failed to load sessions, starting fresh: " + e.getMessage());
      return new ConcurrentHashMap<>();
    }
  }
}

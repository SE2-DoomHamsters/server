package com.doomhamsters.gamesession;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

  private static final Logger LOGGER =
      LoggerFactory.getLogger(GameSessionPersistenceService.class);

  private final String filePath;
  private final ObjectMapper objectMapper;

  /**
   * Creates the persistence service for the configured sessions file.
   *
   * @param filePath path where sessions are stored
   */
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
  public void saveSessions(ConcurrentMap<String, GameSession> sessions) {
    try {
      File file = new File(filePath);
      File parent = file.getParentFile();
      if (parent != null && !parent.exists() && !parent.mkdirs()) {
        LOGGER.warn("Failed to create directory: {}", parent.getAbsolutePath());
        return;
      }
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, sessions);
    } catch (Exception e) {
      LOGGER.warn("Failed to save sessions: {}", e.getMessage());
    }
  }

  /**
   * Loads sessions from disk.
   *
   * @return restored sessions or empty map if file does not exist
   */
  public ConcurrentMap<String, GameSession> loadSessions() {

    File file = new File(filePath);

    if (!file.exists()) {
      return new ConcurrentHashMap<>();
    }

    try {
      return objectMapper.readValue(
          file,
          new TypeReference<ConcurrentHashMap<String, GameSession>>() {});
    } catch (Exception e) {
      LOGGER.warn("Failed to load sessions, starting fresh: {}", e.getMessage());
      return new ConcurrentHashMap<>();
    }
  }
}

package com.doomhamsters.gamesession.dto;

import java.time.Instant;

/**
 * Player-private error event sent when a game action is rejected.
 */
public class ErrorEventDto {

  private final String type = "GAME_ERROR";
  private final String code;
  private final String message;
  private final String gameId;
  private final String timestamp;

  /**
   * Creates an error event.
   *
   * @param code short machine-readable error code
   * @param message human-readable reason the action was rejected
   * @param gameId the affected game session
   */
  public ErrorEventDto(String code, String message, String gameId) {
    this.code = code;
    this.message = message;
    this.gameId = gameId;
    this.timestamp = Instant.now().toString();
  }

  /**
   * Returns the event type.
   *
   * @return event type constant
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the machine-readable error code.
   *
   * @return short machine-readable error code
   */
  public String getCode() {
    return code;
  }

  /**
   * Returns the human-readable rejection reason.
   *
   * @return reason the action was rejected
   */
  public String getMessage() {
    return message;
  }

  /**
   * Returns the affected game session.
   *
   * @return the affected game session id
   */
  public String getGameId() {
    return gameId;
  }

  /**
   * Returns the creation time.
   *
   * @return ISO-8601 creation time
   */
  public String getTimestamp() {
    return timestamp;
  }
}

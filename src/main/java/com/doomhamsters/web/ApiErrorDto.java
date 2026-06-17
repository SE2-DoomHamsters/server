package com.doomhamsters.web;

import java.time.Instant;

/**
 * Unified error response body returned by the REST endpoints.
 */
public class ApiErrorDto {

  private final String timestamp;
  private final int status;
  private final String error;
  private final String message;
  private final String path;

  /**
   * Creates a REST error body.
   *
   * @param status HTTP status code
   * @param error short status reason phrase
   * @param message human-readable detail
   * @param path the request URI that caused the error
   */
  public ApiErrorDto(int status, String error, String message, String path) {
    this.timestamp = Instant.now().toString();
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
  }

  /**
   * Returns the creation time.
   *
   * @return ISO-8601 creation time
   */
  public String getTimestamp() {
    return timestamp;
  }

  /**
   * Returns the HTTP status code.
   *
   * @return HTTP status code
   */
  public int getStatus() {
    return status;
  }

  /**
   * Returns the short status reason phrase.
   *
   * @return status reason phrase
   */
  public String getError() {
    return error;
  }

  /**
   * Returns the human-readable detail message.
   *
   * @return detail message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Returns the request URI that caused the error.
   *
   * @return request path
   */
  public String getPath() {
    return path;
  }
}

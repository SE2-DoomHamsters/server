package com.doomhamsters.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps uncaught exceptions from the REST endpoints to a unified {@link ApiErrorDto} body.
 *
 * <p>Spring's own MVC exceptions (such as method-not-allowed or an unreadable body) keep their
 * original status code and are rendered through {@link #handleExceptionInternal}, so they are
 * never masked as 500.
 */
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

  /**
   * Maps invalid client input to 400 Bad Request.
   *
   * @param exception the offending exception
   * @param request the current request
   * @return a 400 error body
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorDto> handleBadRequest(
      IllegalArgumentException exception, HttpServletRequest request) {

    LOG.warn("bad request: path={}, reason={}", request.getRequestURI(), exception.getMessage());
    return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
  }

  /**
   * Maps an illegal state (an action not allowed right now) to 409 Conflict.
   *
   * @param exception the offending exception
   * @param request the current request
   * @return a 409 error body
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiErrorDto> handleConflict(
      IllegalStateException exception, HttpServletRequest request) {

    LOG.warn("conflict: path={}, reason={}", request.getRequestURI(), exception.getMessage());
    return build(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
  }

  /**
   * Maps any other unexpected error to 500 Internal Server Error.
   *
   * @param exception the offending exception
   * @param request the current request
   * @return a 500 error body
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorDto> handleUnexpected(
      Exception exception, HttpServletRequest request) {

    LOG.error("unexpected error: path={}", request.getRequestURI(), exception);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.",
        request.getRequestURI());
  }

  @Override
  @Nullable
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex,
      @Nullable Object body,
      HttpHeaders headers,
      HttpStatusCode statusCode,
      WebRequest request) {

    String path = (request instanceof ServletWebRequest servletRequest)
        ? servletRequest.getRequest().getRequestURI()
        : "";
    HttpStatus status = HttpStatus.valueOf(statusCode.value());
    LOG.warn("request error: status={}, path={}, reason={}",
        statusCode.value(), path, ex.getMessage());
    ApiErrorDto apiError =
        new ApiErrorDto(statusCode.value(), status.getReasonPhrase(), ex.getMessage(), path);
    return new ResponseEntity<>(apiError, headers, statusCode);
  }

  private ResponseEntity<ApiErrorDto> build(HttpStatus status, String message, String path) {
    ApiErrorDto body = new ApiErrorDto(status.value(), status.getReasonPhrase(), message, path);
    return ResponseEntity.status(status).body(body);
  }
}

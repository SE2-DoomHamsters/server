package com.doomhamsters.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;

class RestExceptionHandlerTest {

  private final RestExceptionHandler handler = new RestExceptionHandler();

  @Test
  void illegalArgumentMapsToBadRequest() {
    ResponseEntity<ApiErrorDto> response =
        handler.handleBadRequest(new IllegalArgumentException("bad input"), request("/api/lobby/create"));

    assertEquals(400, response.getStatusCode().value());
    ApiErrorDto body = response.getBody();
    assertNotNull(body);
    assertEquals(400, body.getStatus());
    assertEquals("Bad Request", body.getError());
    assertEquals("bad input", body.getMessage());
    assertEquals("/api/lobby/create", body.getPath());
    assertNotNull(body.getTimestamp());
  }

  @Test
  void illegalStateMapsToConflict() {
    ResponseEntity<ApiErrorDto> response =
        handler.handleConflict(new IllegalStateException("game already started"),
            request("/api/lobby/abc/join"));

    assertEquals(409, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals("Conflict", response.getBody().getError());
    assertEquals("game already started", response.getBody().getMessage());
  }

  @Test
  void unexpectedMapsToInternalServerError() {
    ResponseEntity<ApiErrorDto> response =
        handler.handleUnexpected(new RuntimeException("kaboom"), request("/api/game/g1/state"));

    assertEquals(500, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals("Internal Server Error", response.getBody().getError());
  }

  @Test
  void frameworkExceptionKeepsStatusWithUnifiedBody() {
    HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    when(httpRequest.getRequestURI()).thenReturn("/api/lobby/create");

    ResponseEntity<Object> response = handler.handleExceptionInternal(
        new RuntimeException("method not allowed"),
        null,
        new HttpHeaders(),
        HttpStatus.METHOD_NOT_ALLOWED,
        new ServletWebRequest(httpRequest));

    assertEquals(405, response.getStatusCode().value());
    ApiErrorDto body = (ApiErrorDto) response.getBody();
    assertNotNull(body);
    assertEquals(405, body.getStatus());
    assertEquals("Method Not Allowed", body.getError());
    assertEquals("/api/lobby/create", body.getPath());
  }

  private HttpServletRequest request(String uri) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn(uri);
    return request;
  }
}

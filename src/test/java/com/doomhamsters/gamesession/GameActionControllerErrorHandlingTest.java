package com.doomhamsters.gamesession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.doomhamsters.gamesession.dto.ErrorCode;
import com.doomhamsters.gamesession.dto.ErrorEventDto;
import com.doomhamsters.gamesession.dto.GameStateMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import tools.jackson.databind.ObjectMapper;

class GameActionControllerErrorHandlingTest {

  private SimpMessagingTemplate messagingTemplate;

  private GameActionController controller;

  @BeforeEach
  void setUp() {
    messagingTemplate = mock(SimpMessagingTemplate.class);

    controller =
        new GameActionController(
            mock(GameSessionService.class),
            new GameStateMapper(),
            messagingTemplate,
            new ObjectMapper());
  }

  @Test
  void rejectedActionSendsErrorEventToOriginatingPlayer() {
    Message<byte[]> message = actionMessage("/app/game/g1/draw", "{\"playerId\":\"p1\"}");

    controller.handleInvalidAction(
        new IllegalArgumentException("It is not player p1's turn."), message);

    ArgumentCaptor<ErrorEventDto> captor = ArgumentCaptor.forClass(ErrorEventDto.class);
    verify(messagingTemplate).convertAndSend(
        eq("/queue/game/g1/p1/errors"),
        captor.capture());

    ErrorEventDto event = captor.getValue();
    assertEquals("GAME_ERROR", event.getType());
    assertEquals(ErrorCode.INVALID_ACTION, event.getCode());
    assertEquals("g1", event.getGameId());
    assertEquals("It is not player p1's turn.", event.getMessage());
  }

  @Test
  void illegalStateUsesIllegalStateCode() {
    Message<byte[]> message = actionMessage("/app/game/g1/nextTurn", "{\"playerId\":\"p1\"}");

    controller.handleInvalidAction(
        new IllegalStateException("Doom resolution must be acknowledged before playing cards."),
        message);

    ArgumentCaptor<ErrorEventDto> captor = ArgumentCaptor.forClass(ErrorEventDto.class);
    verify(messagingTemplate).convertAndSend(
        eq("/queue/game/g1/p1/errors"),
        captor.capture());

    assertEquals(ErrorCode.ILLEGAL_STATE, captor.getValue().getCode());
  }

  @Test
  void malformedPayloadIsLoggedButNotRouted() {
    Message<byte[]> message = actionMessage("/app/game/g1/draw", "not valid json");

    controller.handleInvalidAction(new IllegalArgumentException("Action payload must be valid JSON."),
        message);

    verify(messagingTemplate, never()).convertAndSend(anyString(), any(ErrorEventDto.class));
  }

  @Test
  void missingDestinationIsNotRouted() {
    Message<byte[]> message = MessageBuilder
        .withPayload("{\"playerId\":\"p1\"}".getBytes(StandardCharsets.UTF_8))
        .build();

    controller.handleInvalidAction(new IllegalArgumentException("boom"), message);

    verify(messagingTemplate, never()).convertAndSend(anyString(), any(ErrorEventDto.class));
  }

  @Test
  void missingPlayerIdIsNotRouted() {
    Message<byte[]> message = actionMessage("/app/game/g1/draw", "{}");

    controller.handleInvalidAction(new IllegalArgumentException("boom"), message);

    verify(messagingTemplate, never()).convertAndSend(anyString(), any(ErrorEventDto.class));
  }

  @Test
  void unsupportedPayloadTypeIsNotRouted() {
    Message<Integer> message = MessageBuilder
        .withPayload(123)
        .setHeader(SimpMessageHeaderAccessor.DESTINATION_HEADER, "/app/game/g1/draw")
        .build();

    controller.handleInvalidAction(new IllegalArgumentException("boom"), message);

    verify(messagingTemplate, never()).convertAndSend(anyString(), any(ErrorEventDto.class));
  }

  private Message<byte[]> actionMessage(String destination, String payload) {
    return MessageBuilder
        .withPayload(payload.getBytes(StandardCharsets.UTF_8))
        .setHeader(SimpMessageHeaderAccessor.DESTINATION_HEADER, destination)
        .build();
  }
}

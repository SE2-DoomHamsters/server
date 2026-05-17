package com.doomhamsters.gamesession;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.doomhamsters.Card;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.CardCommandPlayedEventDto;
import com.doomhamsters.gamesession.cardcommands.CardCommandResultEventDto;
import com.doomhamsters.gamesession.dto.DoomDrawnEventDto;
import com.doomhamsters.gamesession.dto.GameStateDto;
import com.doomhamsters.gamesession.dto.GameStateMapper;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tools.jackson.databind.ObjectMapper;

class GameActionControllerTest {

  private GameSessionService gameSessionService;

  private SimpMessagingTemplate messagingTemplate;

  private GameActionController controller;

  @BeforeEach
  void setUp() {
    gameSessionService =
        new GameSessionService(
            new GameSessionPersistenceService());

    messagingTemplate = mock(SimpMessagingTemplate.class);

    controller =
        new GameActionController(
            gameSessionService,
            new GameStateMapper(),
            messagingTemplate,
            new ObjectMapper());
  }

  @Test
  void powerNapDiscardsCardEndsTurnSendsPublicEventAndBroadcastsState() {
    GameSession session = runningSession();
    session.getGame().getBoard().setCurrentIndex(1);
    Player current = session.getGame().getBoard().getCurrentPlayer();
    current.addToHand(new Card("power_1", "Power Nap", "PowerNap"));

    controller.activateCard(
        session.getGameId(),
        """
            {
              "playerId": "p1",
              "cardId": "power_1",
              "cardType": "PowerNap",
              "commandId": "POWER_NAP",
              "parameters": {}
            }
            """);

    GameSession saved =
        gameSessionService
            .getSession(session.getGameId())
            .orElseThrow();

    assertEquals("p0", saved.getGame().getBoard().getCurrentPlayer().getId());
    assertEquals(1, saved.getGame().getBoard().getTurnCount());
    assertFalse(
        current.getHand().stream()
            .anyMatch(card -> card.getId().equals("power_1")));
    assertTrue(
        saved.getGame().getBoard().getDiscardPile().stream()
            .anyMatch(card -> card.getId().equals("power_1")));

    ArgumentCaptor<CardCommandPlayedEventDto> eventCaptor =
        ArgumentCaptor.forClass(CardCommandPlayedEventDto.class);
    ArgumentCaptor<GameStateDto> stateCaptor =
        ArgumentCaptor.forClass(GameStateDto.class);

    InOrder inOrder = inOrder(messagingTemplate);
    inOrder.verify(messagingTemplate).convertAndSend(
        eq("/topic/game/" + session.getGameId()),
        eventCaptor.capture());
    inOrder.verify(messagingTemplate).convertAndSend(
        eq("/topic/game-state/" + session.getGameId()),
        stateCaptor.capture());

    CardCommandPlayedEventDto event = eventCaptor.getValue();
    assertEquals("CARD_COMMAND_PLAYED", event.getType());
    assertEquals("p1", event.getPlayerId());
    assertEquals("ty", event.getPlayerName());
    assertEquals("POWER_NAP", event.getCommandId());
    assertEquals("power_1", event.getCard().getId());
    assertEquals("PowerNap", event.getCard().getType());
    assertEquals("Power Nap", event.getCard().getName());
    assertEquals("POWER_NAP", event.getCard().getEffectId());
    assertEquals("ty activated Power Nap and skipped drawing.", event.getMessage());

    assertEquals("p0", stateCaptor.getValue().getCurrentPlayerId());
    assertEquals(1, stateCaptor.getValue().getTurnCount());
  }

  @Test
  void quickPeekDiscardsCardKeepsTurnSendsPublicPrivateEventsAndBroadcastsState() {
    GameSession session = runningSession();
    session.getGame().getBoard().setCurrentIndex(1);
    Player current = session.getGame().getBoard().getCurrentPlayer();
    current.addToHand(new Card("peek_1", "Quick Peek", "QuickPeek"));

    while (!session.getGame().getDeck().isEmpty()) {
      session.getGame().getDeck().draw();
    }
    session.getGame().getDeck().insertDoomCards(
        List.of(new Card("doom_top", "Doom", "doom")));

    controller.activateCard(
        session.getGameId(),
        """
            {
              "playerId": "p1",
              "cardId": "peek_1",
              "cardType": "QuickPeek",
              "commandId": "QUICK_PEEK",
              "parameters": {}
            }
            """);

    GameSession saved =
        gameSessionService
            .getSession(session.getGameId())
            .orElseThrow();

    assertEquals("p1", saved.getGame().getBoard().getCurrentPlayer().getId());
    assertEquals(0, saved.getGame().getBoard().getTurnCount());
    assertEquals("doom_top", saved.getGame().getDeck().getCards().get(0).getId());
    assertTrue(
        saved.getGame().getBoard().getDiscardPile().stream()
            .anyMatch(card -> card.getId().equals("peek_1")));

    ArgumentCaptor<CardCommandPlayedEventDto> publicCaptor =
        ArgumentCaptor.forClass(CardCommandPlayedEventDto.class);
    ArgumentCaptor<CardCommandResultEventDto> privateCaptor =
        ArgumentCaptor.forClass(CardCommandResultEventDto.class);
    ArgumentCaptor<GameStateDto> stateCaptor =
        ArgumentCaptor.forClass(GameStateDto.class);

    InOrder inOrder = inOrder(messagingTemplate);
    inOrder.verify(messagingTemplate).convertAndSend(
        eq("/topic/game/" + session.getGameId()),
        publicCaptor.capture());
    inOrder.verify(messagingTemplate).convertAndSend(
        eq("/queue/game/" + session.getGameId() + "/p1"),
        privateCaptor.capture());
    inOrder.verify(messagingTemplate).convertAndSend(
        eq("/topic/game-state/" + session.getGameId()),
        stateCaptor.capture());

    assertEquals("CARD_COMMAND_PLAYED", publicCaptor.getValue().getType());
    assertEquals("QUICK_PEEK", publicCaptor.getValue().getCommandId());
    assertEquals("peek_1", publicCaptor.getValue().getCard().getId());
    assertEquals("QuickPeek", publicCaptor.getValue().getCard().getType());
    assertEquals("Quick Peek", publicCaptor.getValue().getCard().getName());
    assertEquals("QUICK_PEEK", publicCaptor.getValue().getCard().getEffectId());
    assertEquals("ty activated Quick Peek.", publicCaptor.getValue().getMessage());

    CardCommandResultEventDto privateEvent = privateCaptor.getValue();
    assertEquals("CARD_COMMAND_RESULT", privateEvent.getType());
    assertEquals("p1", privateEvent.getPlayerId());
    assertEquals("QUICK_PEEK", privateEvent.getCommandId());
    assertEquals("peek_1", privateEvent.getCard().getId());
    assertEquals("QuickPeek", privateEvent.getCard().getType());
    assertEquals("Quick Peek", privateEvent.getCard().getName());
    assertEquals("QUICK_PEEK", privateEvent.getCard().getEffectId());
    assertEquals("doom_top", privateEvent.getRevealedCard().getId());
    assertEquals("Top card: Doom.", privateEvent.getMessage());

    assertEquals("p1", stateCaptor.getValue().getCurrentPlayerId());
    assertEquals(0, stateCaptor.getValue().getTurnCount());
  }

  @Test
  void drawMutatesSavedSessionAndBroadcastsUpdatedState() {
    GameSession session = runningSession();
    session.getGame().getBoard().setCurrentIndex(1);
    int initialHandSize = session.getGame().getBoard().getCurrentPlayer().getHand().size();

    controller.draw(session.getGameId(), "{\"playerId\":\"p1\"}");

    GameSession saved =
        gameSessionService
            .getSession(session.getGameId())
            .orElseThrow();

    assertEquals(
        initialHandSize + 1,
        saved.getGame().getBoard().getCurrentPlayer().getHand().size());
    assertEquals("p1", saved.getGame().getBoard().getCurrentPlayer().getId());
    assertEquals(0, saved.getGame().getBoard().getTurnCount());

    ArgumentCaptor<GameStateDto> stateCaptor =
        ArgumentCaptor.forClass(GameStateDto.class);

    verify(messagingTemplate).convertAndSend(
        eq("/topic/game-state/" + session.getGameId()),
        stateCaptor.capture());

    GameStateDto broadcastState = stateCaptor.getValue();
    assertEquals("p1", broadcastState.getCurrentPlayerId());
    assertEquals(0, broadcastState.getTurnCount());
  }

  @Test
  void nextTurnMutatesSavedSessionAndBroadcastsUpdatedState() {
    GameSession session = runningSession();
    session.getGame().getBoard().setCurrentIndex(1);

    controller.nextTurn(session.getGameId());

    GameSession saved =
        gameSessionService
            .getSession(session.getGameId())
            .orElseThrow();

    assertEquals("p0", saved.getGame().getBoard().getCurrentPlayer().getId());
    assertEquals(1, saved.getGame().getBoard().getTurnCount());

    ArgumentCaptor<GameStateDto> stateCaptor =
        ArgumentCaptor.forClass(GameStateDto.class);

    verify(messagingTemplate).convertAndSend(
        eq("/topic/game-state/" + session.getGameId()),
        stateCaptor.capture());

    GameStateDto broadcastState = stateCaptor.getValue();
    assertEquals("p0", broadcastState.getCurrentPlayerId());
    assertEquals(1, broadcastState.getTurnCount());
  }

  @Test
  void drawRejectsNonCurrentPlayer() {
    GameSession session = runningSession();
    session.getGame().getBoard().setCurrentIndex(1);

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.draw(session.getGameId(), "{\"playerId\":\"p0\"}"));
  }

  @Test
  void drawDoomSendsPrivateEventBeforeSharedState() {
    GameSession session = runningSession();
    session.getGame().getBoard().setCurrentIndex(1);
    Player current = session.getGame().getBoard().getCurrentPlayer();

    for (Card card : new ArrayList<>(current.getHand())) {
      if (card.isSnackStash()) {
        current.removeFromHand(card.getId());
      }
    }

    while (!session.getGame().getDeck().isEmpty()) {
      session.getGame().getDeck().draw();
    }

    session.getGame().getDeck().insertDoomCards(
        List.of(new Card("doom_android", "Doom Hamster", "doom")));

    int remainingDeckSizeBefore = session.getGame().getDeck().size();

    controller.draw(session.getGameId(), "{\"playerId\":\"p1\"}");

    ArgumentCaptor<DoomDrawnEventDto> eventCaptor =
        ArgumentCaptor.forClass(DoomDrawnEventDto.class);
    ArgumentCaptor<GameStateDto> stateCaptor =
        ArgumentCaptor.forClass(GameStateDto.class);

    InOrder inOrder = inOrder(messagingTemplate);
    inOrder.verify(messagingTemplate).convertAndSend(
        eq("/queue/game/" + session.getGameId() + "/p1"),
        eventCaptor.capture());
    inOrder.verify(messagingTemplate).convertAndSend(
        eq("/topic/game-state/" + session.getGameId()),
        stateCaptor.capture());

    DoomDrawnEventDto event = eventCaptor.getValue();
    assertEquals("DOOM_DRAWN", event.getType());
    assertEquals("doom_android", event.getCard().getId());
    assertEquals("Doom Hamster", event.getCard().getName());
    assertEquals("doom", event.getCard().getType());

    assertEquals("p1", stateCaptor.getValue().getCurrentPlayerId());
    assertEquals("p1", stateCaptor.getValue().getResolvingDoomPlayerId());
    assertFalse(stateCaptor.getValue().isPendingDoomRequiresInsertion());
    assertEquals(remainingDeckSizeBefore, stateCaptor.getValue().getRemainingDeckSize());

    GameSession saved =
        gameSessionService
            .getSession(session.getGameId())
            .orElseThrow();

    assertTrue(saved.getGame().getDeck().getCards().stream()
        .anyMatch(card -> card.getId().equals("doom_android")));
    assertFalse(saved.getGame().getDeck().getDiscards().stream()
        .anyMatch(card -> card.getId().equals("doom_android")));
  }

  @Test
  void drawDoomWithSnackStashRequiresInsertionChoice() {
    GameSession session = runningSession();
    session.getGame().getBoard().setCurrentIndex(1);
    Player current = session.getGame().getBoard().getCurrentPlayer();
    int handSizeBefore = current.getHand().size();

    while (!session.getGame().getDeck().isEmpty()) {
      session.getGame().getDeck().draw();
    }

    session.getGame().getDeck().insertDoomCards(
        List.of(new Card("doom_insert", "Doom Hamster", "doom")));

    controller.draw(session.getGameId(), "{\"playerId\":\"p1\"}");

    ArgumentCaptor<GameStateDto> stateCaptor =
        ArgumentCaptor.forClass(GameStateDto.class);

    verify(messagingTemplate).convertAndSend(
        eq("/topic/game-state/" + session.getGameId()),
        stateCaptor.capture());

    GameSession saved =
        gameSessionService
            .getSession(session.getGameId())
            .orElseThrow();

    assertEquals(
        handSizeBefore - 1,
        saved.getGame().getBoard().getCurrentPlayer().getHand().size());
    assertEquals("p1", stateCaptor.getValue().getResolvingDoomPlayerId());
    assertTrue(stateCaptor.getValue().isPendingDoomRequiresInsertion());
    assertEquals("doom_insert", stateCaptor.getValue().getPendingDoomCardId());
    assertEquals("doom_insert", saved.getGame().getPendingDoomCardId());
  }

  @Test
  void insertDoomReinsertsPendingCardClearsStateAndBroadcasts() {
    GameSession session = runningSession();
    session.getGame().getBoard().setCurrentIndex(1);

    while (!session.getGame().getDeck().isEmpty()) {
      session.getGame().getDeck().draw();
    }

    session.getGame().getDeck().insertDoomCards(
        List.of(new Card("doom_insert", "Doom Hamster", "doom")));

    controller.draw(session.getGameId(), "{\"playerId\":\"p1\"}");
    controller.insertDoom(session.getGameId(), "{\"playerId\":\"p1\",\"position\":0}");

    GameSession saved =
        gameSessionService
            .getSession(session.getGameId())
            .orElseThrow();

    assertEquals("doom_insert", saved.getGame().getDeck().getCards().get(0).getId());
    assertNull(saved.getGame().getResolvingDoomPlayerId());
    assertFalse(saved.getGame().isPendingDoomRequiresInsertion());
    assertNull(saved.getGame().getPendingDoomCardId());

    ArgumentCaptor<GameStateDto> stateCaptor =
        ArgumentCaptor.forClass(GameStateDto.class);

    verify(messagingTemplate, times(2)).convertAndSend(
        eq("/topic/game-state/" + session.getGameId()),
        stateCaptor.capture());

    GameStateDto insertedState = stateCaptor.getAllValues().get(1);
    assertNull(insertedState.getResolvingDoomPlayerId());
    assertFalse(insertedState.isPendingDoomRequiresInsertion());
    assertNull(insertedState.getPendingDoomCardId());
  }

  @Test
  void ackDoomRejectsWhenDoomInsertionIsPending() {
    GameSession session = runningSession();
    session.getGame().setResolvingDoomPlayerId("p1");
    session.getGame().setPendingDoomCard(new Card("doom_insert", "Doom Hamster", "doom"));

    assertThrows(
        IllegalStateException.class,
        () -> controller.ackDoom(session.getGameId(), "{\"playerId\":\"p1\"}"));
  }

  @Test
  void ackDoomClearsResolvingDoomPlayerAndBroadcastsState() {
    GameSession session = runningSession();
    session.getGame().setResolvingDoomPlayerId("p1");

    controller.ackDoom(session.getGameId(), "{\"playerId\":\"p1\"}");

    ArgumentCaptor<GameStateDto> stateCaptor =
        ArgumentCaptor.forClass(GameStateDto.class);

    verify(messagingTemplate).convertAndSend(
        eq("/topic/game-state/" + session.getGameId()),
        stateCaptor.capture());

    assertEquals(session.getGameId(), stateCaptor.getValue().getGameId());
    assertNull(stateCaptor.getValue().getResolvingDoomPlayerId());
    assertFalse(stateCaptor.getValue().isPendingDoomRequiresInsertion());
    assertNull(stateCaptor.getValue().getPendingDoomCardId());
  }

  @Test
  void nextTurnRejectsWhileDoomResolutionIsPending() {
    GameSession session = runningSession();
    session.getGame().getBoard().setCurrentIndex(1);
    session.getGame().setResolvingDoomPlayerId("p1");

    assertThrows(
        IllegalStateException.class,
        () -> controller.nextTurn(session.getGameId()));

    GameSession saved =
        gameSessionService
            .getSession(session.getGameId())
            .orElseThrow();

    assertEquals("p1", saved.getGame().getBoard().getCurrentPlayer().getId());
    assertEquals(0, saved.getGame().getBoard().getTurnCount());
    assertEquals("p1", saved.getGame().getResolvingDoomPlayerId());
  }

  @Test
  void nextTurnRejectsWhileDoomInsertionIsPending() {
    GameSession session = runningSession();
    session.getGame().getBoard().setCurrentIndex(1);
    session.getGame().setPendingDoomCard(new Card("doom_insert", "Doom Hamster", "doom"));

    assertThrows(
        IllegalStateException.class,
        () -> controller.nextTurn(session.getGameId()));

    GameSession saved =
        gameSessionService
            .getSession(session.getGameId())
            .orElseThrow();

    assertEquals("p1", saved.getGame().getBoard().getCurrentPlayer().getId());
    assertEquals(0, saved.getGame().getBoard().getTurnCount());
    assertTrue(saved.getGame().isPendingDoomRequiresInsertion());
  }

  @Test
  void insertDoomRejectsWrongPlayer() {
    GameSession session = runningSession();
    session.getGame().setResolvingDoomPlayerId("p1");
    session.getGame().setPendingDoomCard(new Card("doom_insert", "Doom Hamster", "doom"));

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.insertDoom(session.getGameId(), "{\"playerId\":\"p0\",\"position\":0}"));
  }

  @Test
  void insertDoomRejectsOutOfBoundsPosition() {
    GameSession session = runningSession();
    session.getGame().setResolvingDoomPlayerId("p1");
    session.getGame().setPendingDoomCard(new Card("doom_insert", "Doom Hamster", "doom"));

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.insertDoom(session.getGameId(), "{\"playerId\":\"p1\",\"position\":999}"));
  }

  @Test
  void drawUsesDrawMessageMapping() throws Exception {
    Method method =
        GameActionController.class.getMethod(
            "draw",
            String.class,
            String.class);

    MessageMapping mapping = method.getAnnotation(MessageMapping.class);

    assertArrayEquals(
        new String[] {"/game/{gameId}/draw"},
        mapping.value());
  }

  @Test
  void activateCardUsesActivateCardMessageMapping() throws Exception {
    Method method =
        GameActionController.class.getMethod(
            "activateCard",
            String.class,
            String.class);

    MessageMapping mapping = method.getAnnotation(MessageMapping.class);

    assertArrayEquals(
        new String[] {"/game/{gameId}/card/activate"},
        mapping.value());
  }

  @Test
  void nextTurnUsesNextTurnMessageMapping() throws Exception {
    Method method =
        GameActionController.class.getMethod(
            "nextTurn",
            String.class);

    MessageMapping mapping = method.getAnnotation(MessageMapping.class);

    assertArrayEquals(
        new String[] {"/game/{gameId}/nextTurn"},
        mapping.value());
  }

  @Test
  void ackDoomUsesAckDoomMessageMapping() throws Exception {
    Method method =
        GameActionController.class.getMethod(
            "ackDoom",
            String.class,
            String.class);

    MessageMapping mapping = method.getAnnotation(MessageMapping.class);

    assertArrayEquals(
        new String[] {"/game/{gameId}/doom/ack"},
        mapping.value());
  }

  @Test
  void insertDoomUsesInsertDoomMessageMapping() throws Exception {
    Method method =
        GameActionController.class.getMethod(
            "insertDoom",
            String.class,
            String.class);

    MessageMapping mapping = method.getAnnotation(MessageMapping.class);

    assertArrayEquals(
        new String[] {"/game/{gameId}/doom/insert"},
        mapping.value());
  }

  @Test
  void acceptDoomUsesAcceptDoomMessageMapping() throws Exception {
    Method method =
        GameActionController.class.getMethod(
            "acceptDoom",
            String.class,
            String.class);

    MessageMapping mapping = method.getAnnotation(MessageMapping.class);

    assertArrayEquals(
        new String[] {"/game/{gameId}/doom/accept"},
        mapping.value());
  }

  private GameSession runningSession() {
    GameSession session = gameSessionService.createSession("lobby-1");

    session.getGame().setup(
        List.of("yy", "ty"),
        actionCards(20),
        new Card("ss_proto", "Snack Stash", "snack_stash"),
        List.of(new Card("doom_1", "Doom Hamster", "doom")));

    session.setStatus(GameSession.GameStatus.RUNNING);
    gameSessionService.saveSession(session);

    return session;
  }

  private List<Card> actionCards(int count) {
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      cards.add(new Card("act_" + i, "Action " + i, "action"));
    }

    return cards;
  }
}

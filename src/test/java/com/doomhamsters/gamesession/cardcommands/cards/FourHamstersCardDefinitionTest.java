package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FourHamstersCardDefinitionTest {

  private FourHamstersCardDefinition cardDefinition;
  private Player initiator;
  private Player target;
  private Game gameMock;
  private GameSession sessionMock;
  private Card dummyCard;

  @BeforeEach
  void setUp() {
    cardDefinition = new FourHamstersCardDefinition();

    // Spieler starten mit 3 Leben
    initiator = new Player("initiator-id", "Alice");
    target = new Player("target-id", "Bob");

    gameMock = mock(Game.class);
    sessionMock = mock(GameSession.class);
    dummyCard = new Card("card-1", "Hamster Combo", "Normal");

    when(gameMock.getPlayers()).thenReturn(List.of(initiator, target));
  }

  @Test
  void execute_shouldStealLife_whenTargetSurvives() {
    Map<String, Object> params = Map.of("targetPlayerId", target.getId());
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    CardCommandResult result = cardDefinition.execute(context);

    assertEquals(2, target.getLives(), "Target sollte 1 Leben verlieren (3 -> 2).");
    assertFalse(target.isEliminated(), "Target sollte nicht eliminiert sein.");
    assertEquals(4, initiator.getLives(), "Initiator sollte 1 Leben gewinnen (3 -> 4).");

    assertTrue(result.getPublicMessage().contains("stole 1 life from Bob!"));
    assertFalse(result.getPublicMessage().contains("eliminated"));
  }

  @Test
  void execute_shouldStealLife_andTargetIsEliminated() {
    target.decrementLives();
    target.decrementLives();

    Map<String, Object> params = Map.of("targetPlayerId", target.getId());
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    CardCommandResult result = cardDefinition.execute(context);

    assertEquals(0, target.getLives(), "Target sollte auf 0 Leben fallen.");
    assertTrue(target.isEliminated(), "Target sollte eliminiert sein.");
    assertEquals(4, initiator.getLives(), "Initiator sollte 1 Leben gewinnen.");

    assertTrue(result.getPublicMessage().contains("Bob was eliminated!"));
  }

  @Test
  void execute_shouldThrowException_whenTargetIdIsMissing() {
    Map<String, Object> params = Map.of();
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> cardDefinition.execute(context)
    );
    assertTrue(exception.getMessage().contains("Target player ID is required"));
  }

  @Test
  void execute_shouldThrowException_whenTargetNotFound() {
    Map<String, Object> params = Map.of("targetPlayerId", "unknown-id");
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> cardDefinition.execute(context)
    );
    assertTrue(exception.getMessage().contains("Target player not found."));
  }

  @Test
  void execute_shouldThrowException_whenTargetAlreadyEliminated() {
    target.decrementLives();
    target.decrementLives();
    target.decrementLives();

    Map<String, Object> params = Map.of("targetPlayerId", target.getId());
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> cardDefinition.execute(context)
    );
    assertTrue(exception.getMessage().contains("already eliminated."));
  }
  @Test
  void cardType_shouldReturnNormal() {
    assertEquals("Normal", cardDefinition.cardType(), "Card type should be Normal.");
  }

  @Test
  void commandId_shouldReturnFourHamsters() {
    assertEquals("FOUR_HAMSTERS", cardDefinition.commandId(), "Command ID should match.");
  }

  @Test
  void displayName_shouldReturnCorrectName() {
    assertEquals("Hamster Combo: 4-of-a-Kind — Steal 1 Life", cardDefinition.displayName(), "Display name should match the required UI string.");
  }

  @Test
  void createTestingCard_shouldCreateValidCardForPlayer() {
    // Act
    Card testCard = cardDefinition.createTestingCard("test-player-1");

    // Assert
    assertEquals("four_hamsters_test-player-1", testCard.getId(), "Card ID should include the player ID.");
    assertEquals("Hamster Combo: 4-of-a-Kind — Steal 1 Life", testCard.getName(), "Card name should match display name.");
    assertEquals("Normal", testCard.getType(), "Card type should match.");
  }
}

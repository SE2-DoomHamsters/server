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

    // Players start with 3 lives
    initiator = new Player("initiator-id", "Alice");
    target = new Player("target-id", "Bob");

    gameMock = mock(Game.class);
    sessionMock = mock(GameSession.class);
    dummyCard = new Card("card-1", "Hamster Combo", "HAMSTER_FOUR");

    when(gameMock.getPlayers()).thenReturn(List.of(initiator, target));
  }

  // Helper method to give the initiator the required cards in hand
  private void giveInitiatorFourHamsters(String type) {
    for (int i = 0; i < 4; i++) {
      initiator.addToHand(new Card("hamster-" + i, "Hamster", type));
    }
  }

  @Test
  void execute_shouldStealLife_AndDiscardCards_whenTargetSurvives() {
    giveInitiatorFourHamsters("hamster_ninja");
    Map<String, Object> params = Map.of("targetPlayerId", target.getId(), "hamsterType", "hamster_ninja");
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    CardCommandResult result = cardDefinition.execute(context);

    // AC: Target loses 1 life, playing player gains 1 life
    assertEquals(2, target.getLives(), "Target should lose 1 life (3 -> 2).");
    assertFalse(target.isEliminated(), "Target should not be eliminated.");
    assertEquals(4, initiator.getLives(), "Initiator should gain 1 life (3 -> 4).");

    assertEquals(0, initiator.getHand().size(), "Initiator should have discarded the 4 cards.");
    assertTrue(result.getPublicMessage().contains("stole 1 life from Bob!"));
    assertFalse(result.getPublicMessage().contains("eliminated"));
  }

  @Test
  void execute_shouldStealLife_andTargetIsEliminated() {
    giveInitiatorFourHamsters("hamster_fat");
    target.decrementLives();
    target.decrementLives(); // Target has only 1 life left now

    Map<String, Object> params = Map.of("targetPlayerId", target.getId(), "hamsterType", "hamster_fat");
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    CardCommandResult result = cardDefinition.execute(context);

    // AC: Target eliminated if reaches 0 lives
    assertEquals(0, target.getLives(), "Target should drop to 0 lives.");
    assertTrue(target.isEliminated(), "Target should be eliminated.");

    // AC: Playing player gains 1 life
    assertEquals(4, initiator.getLives(), "Initiator should gain 1 life.");

    assertEquals(0, initiator.getHand().size(), "Initiator should have discarded the 4 cards.");
    assertTrue(result.getPublicMessage().contains("Bob was eliminated!"));
  }

  @Test
  void execute_shouldThrowException_whenNotEnoughCardsInHand() {
    // Initiator only has 3 instead of 4 cards!
    for (int i = 0; i < 3; i++) {
      initiator.addToHand(new Card("hamster-" + i, "Hamster", "hamster_ninja"));
    }

    Map<String, Object> params = Map.of("targetPlayerId", target.getId(), "hamsterType", "hamster_ninja");
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> cardDefinition.execute(context)
    );
    assertTrue(exception.getMessage().contains("requester does not hold 4 cards"));

    // IMPORTANT: The 3 cards must not be removed if an error occurs!
    assertEquals(3, initiator.getHand().size(), "The cards must not be discarded if the combo fails.");
  }

  @Test
  void execute_shouldThrowException_whenTargetIdIsMissing() {
    Map<String, Object> params = Map.of("hamsterType", "hamster_ninja");
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> cardDefinition.execute(context)
    );
    assertTrue(exception.getMessage().contains("missing required parameter: targetPlayerId"));
  }

  @Test
  void execute_shouldThrowException_whenHamsterTypeIsMissing() {
    Map<String, Object> params = Map.of("targetPlayerId", target.getId());
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> cardDefinition.execute(context)
    );
    assertTrue(exception.getMessage().contains("missing required parameter: hamsterType"));
  }

  @Test
  void execute_shouldThrowException_whenHamsterTypeIsInvalid() {
    Map<String, Object> params = Map.of("targetPlayerId", target.getId(), "hamsterType", "doom_card");
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> cardDefinition.execute(context)
    );
    assertTrue(exception.getMessage().contains("must start with 'hamster_'"));
  }

  @Test
  void execute_shouldThrowException_whenTargetIsSelf() {
    Map<String, Object> params = Map.of("targetPlayerId", initiator.getId(), "hamsterType", "hamster_ninja");
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> cardDefinition.execute(context)
    );
    assertTrue(exception.getMessage().contains("cannot target yourself"));
  }

  @Test
  void execute_shouldThrowException_whenTargetAlreadyEliminated() {
    giveInitiatorFourHamsters("hamster_ninja");
    target.decrementLives();
    target.decrementLives();
    target.decrementLives(); // Target is eliminated

    Map<String, Object> params = Map.of("targetPlayerId", target.getId(), "hamsterType", "hamster_ninja");
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> cardDefinition.execute(context)
    );
    assertTrue(exception.getMessage().contains("already eliminated."));
  }

  // --- Metadata Tests ---

  @Test
  void cardType_shouldReturnHamsterFour() {
    assertEquals("HAMSTER_FOUR", cardDefinition.cardType());
  }

  @Test
  void commandId_shouldReturnFourHamsters() {
    assertEquals("FOUR_HAMSTERS", cardDefinition.commandId());
  }

  @Test
  void displayName_shouldReturnCorrectName() {
    assertEquals("Hamster Combo: 4-of-a-Kind", cardDefinition.displayName());
  }

  @Test
  void createTestingCard_shouldCreateValidCardForPlayer() {
    Card testCard = cardDefinition.createTestingCard("test-player-1");
    assertEquals("four_hamsters_test-player-1", testCard.getId());
    assertEquals("Hamster Combo: 4-of-a-Kind", testCard.getName());
    assertEquals("HAMSTER_FOUR", testCard.getType());
  }
}

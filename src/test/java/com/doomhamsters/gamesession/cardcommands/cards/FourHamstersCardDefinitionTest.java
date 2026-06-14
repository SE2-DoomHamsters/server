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
    dummyCard = new Card("card-1", "Hamster Combo", "hamster_four");

    when(gameMock.getPlayers()).thenReturn(List.of(initiator, target));
  }

  // Hilfsmethode, um dem Initiator die nötigen Karten auf die Hand zu geben
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
    assertEquals(2, target.getLives(), "Target sollte 1 Leben verlieren (3 -> 2).");
    assertFalse(target.isEliminated(), "Target sollte nicht eliminiert sein.");
    assertEquals(4, initiator.getLives(), "Initiator sollte 1 Leben gewinnen (3 -> 4).");

    assertEquals(0, initiator.getHand().size(), "Initiator sollte die 4 Karten abgeworfen haben.");
    assertTrue(result.getPublicMessage().contains("stole 1 life from Bob!"));
    assertFalse(result.getPublicMessage().contains("eliminated"));
  }

  @Test
  void execute_shouldStealLife_andTargetIsEliminated() {
    giveInitiatorFourHamsters("hamster_fat");
    target.decrementLives();
    target.decrementLives(); // Target hat jetzt nur noch 1 Leben

    Map<String, Object> params = Map.of("targetPlayerId", target.getId(), "hamsterType", "hamster_fat");
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    CardCommandResult result = cardDefinition.execute(context);

    // AC: Target eliminated if reaches 0 lives
    assertEquals(0, target.getLives(), "Target sollte auf 0 Leben fallen.");
    assertTrue(target.isEliminated(), "Target sollte eliminiert sein.");

    // AC: Playing player gains 1 life (Wurde vorher vergessen!)
    assertEquals(4, initiator.getLives(), "Initiator sollte 1 Leben gewinnen.");

    assertEquals(0, initiator.getHand().size(), "Initiator sollte die 4 Karten abgeworfen haben.");
    assertTrue(result.getPublicMessage().contains("Bob was eliminated!"));
  }

  @Test
  void execute_shouldThrowException_whenNotEnoughCardsInHand() {
    // Initiator hat nur 3 statt 4 Karten!
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

    // WICHTIG: Die 3 Karten dürfen bei einem Fehler nicht gelöscht werden!
    assertEquals(3, initiator.getHand().size(), "Die Karten dürfen nicht abgeworfen werden, wenn die Kombo fehlschlägt.");
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
    target.decrementLives(); // Target ist eliminiert

    Map<String, Object> params = Map.of("targetPlayerId", target.getId(), "hamsterType", "hamster_ninja");
    CardCommandContext context = new CardCommandContext(sessionMock, gameMock, initiator, dummyCard, params);

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> cardDefinition.execute(context)
    );
    assertTrue(exception.getMessage().contains("already eliminated."));
  }

  // --- Metadaten Tests ---

  @Test
  void cardType_shouldReturnHamsterFour() {
    assertEquals("hamster_four", cardDefinition.cardType());
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
    assertEquals("hamster_four", testCard.getType());
  }
}

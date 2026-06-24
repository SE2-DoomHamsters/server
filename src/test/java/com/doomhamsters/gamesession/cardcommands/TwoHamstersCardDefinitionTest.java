package com.doomhamsters.gamesession.cardcommands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.cards.TwoHamstersCardDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TwoHamstersCardDefinitionTest {

  private TwoHamstersCardDefinition definition;
  private CardCommandContext context;
  private Game game;
  private Player requester;
  private Player target;

  @BeforeEach
  void setUp() {
    definition = new TwoHamstersCardDefinition();
    context = mock(CardCommandContext.class);
    game = mock(Game.class);
    requester = mock(Player.class);
    target = mock(Player.class);

    when(context.getPlayer()).thenReturn(requester);
    when(context.getGame()).thenReturn(game);

    when(requester.getId()).thenReturn("req-123");
    when(requester.getName()).thenReturn("Alice");

    when(target.getId()).thenReturn("tgt-456");
    when(target.getName()).thenReturn("Bob");
    when(target.isEliminated()).thenReturn(false);

    when(game.getPlayers()).thenReturn(List.of(requester, target));
  }

  @Test
  void testMetadata() {
    assertEquals("HamsterTwo", definition.cardType());
    assertEquals("TWO_HAMSTERS", definition.commandId());
    assertEquals("Hamster Combo: 2-of-a-Kind", definition.displayName());
  }

  @Test
  void testCreateTestingCard() {
    Card testingCard = definition.createTestingCard("player1");
    assertNotNull(testingCard);
    assertEquals("two_hamsters_player1", testingCard.getId());
    assertEquals("Hamster Combo: 2-of-a-Kind", testingCard.getName());
    assertEquals("HamsterTwo", testingCard.getType());
  }

  @Test
  void testFailsIfTargetPlayerIdIsMissing() {
    when(context.getParameters()).thenReturn(Map.of("hamsterType", "hamster_ninja"));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> definition.execute(context));
    assertEquals("TWO_HAMSTERS: missing required parameter: targetPlayerId", exception.getMessage());
  }

  @Test
  void testFailsIfHamsterTypeIsMissing() {
    when(context.getParameters()).thenReturn(Map.of("targetPlayerId", "tgt-456"));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> definition.execute(context));
    assertEquals("TWO_HAMSTERS: missing required parameter: hamsterType", exception.getMessage());
  }

  @Test
  void testFailsIfHamsterTypeIsInvalid() {
    when(context.getParameters()).thenReturn(Map.of(
      "targetPlayerId", "tgt-456",
      "hamsterType", "invalid_type"
    ));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> definition.execute(context));
    assertEquals("TwoHamsters: hamsterType must start with 'hamster_', got: invalid_type",
      exception.getMessage());
  }

  @Test
  void testFailsIfTargetIsUnknown() {
    when(context.getParameters()).thenReturn(Map.of(
      "targetPlayerId", "ghost-789",
      "hamsterType", "hamster_ninja"
    ));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> definition.execute(context));
    assertEquals("TwoHamsters: unknown targetPlayerId=ghost-789", exception.getMessage());
  }

  @Test
  void testFailsIfTargetIsSelf() {
    when(context.getParameters()).thenReturn(Map.of(
      "targetPlayerId", "req-123",
      "hamsterType", "hamster_ninja"
    ));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> definition.execute(context));
    assertEquals("TwoHamsters: cannot target yourself", exception.getMessage());
  }

  @Test
  void testFailsIfTargetIsEliminated() {
    when(target.isEliminated()).thenReturn(true);
    when(context.getParameters()).thenReturn(Map.of(
      "targetPlayerId", "tgt-456",
      "hamsterType", "hamster_ninja"
    ));

    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> definition.execute(context));
    assertEquals("TwoHamsters: Target player is already eliminated.", exception.getMessage());
  }

  @Test
  void testFailsIfRequesterDoesNotHaveEnoughCards() {
    when(context.getParameters()).thenReturn(Map.of(
      "targetPlayerId", "tgt-456",
      "hamsterType", "hamster_ninja"
    ));

    Card hamsterCard = new Card("card1", "Ninja", "hamster_ninja");
    when(requester.getHand()).thenReturn(List.of(hamsterCard));

    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> definition.execute(context));
    assertEquals("TwoHamsters: requester does not hold 2 cards of type hamster_ninja",
      exception.getMessage());
  }

  @Test
  void testReturnsPublicMessageIfTargetHasNoCards() {
    when(context.getParameters()).thenReturn(Map.of(
      "targetPlayerId", "tgt-456",
      "hamsterType", "hamster_ninja"
    ));

    Card hamsterCard1 = new Card("card1", "Ninja", "hamster_ninja");
    Card hamsterCard2 = new Card("card2", "Ninja", "hamster_ninja");
    when(requester.getHand()).thenReturn(List.of(hamsterCard1, hamsterCard2));

    when(target.getHand()).thenReturn(new ArrayList<>());

    CardCommandResult result = definition.execute(context);

    assertNotNull(result);
    verify(requester, times(2)).removeFromHand(anyString());
  }

  @ParameterizedTest
  @ValueSource(strings = {"hamster_ninja", "hamster_viking", "hamster_wizard", "hamster_knight", "hamster_pirate"})
  void testExecuteSuccessfullyStealsCardForAllHamsterTypes(String hamsterType) {
    when(context.getParameters()).thenReturn(Map.of(
      "targetPlayerId", "tgt-456",
      "hamsterType", hamsterType
    ));

    Card hamsterCard1 = new Card("card1", "Hamster", hamsterType);
    Card hamsterCard2 = new Card("card2", "Hamster", hamsterType);
    when(requester.getHand()).thenReturn(List.of(hamsterCard1, hamsterCard2));

    Card targetCard = new Card("targetCard1", "Valuable Loot", "loot");
    when(target.getHand()).thenReturn(List.of(targetCard));
    when(target.removeFromHand("targetCard1")).thenReturn(targetCard);

    CardCommandResult result = definition.execute(context);

    assertNotNull(result);
    // Verifies that the two combo cards were discarded
    verify(requester, times(2)).removeFromHand(anyString());

    // Verifies that a card was stolen from the target
    verify(target, times(1)).removeFromHand(anyString());

    // Verifies that the requester receives the stolen card
    verify(requester, times(1)).addToHand(targetCard);
  }
  @Test
  void testFailsIfParameterIsBlank() {
    // Arrange: The parameter exists but is blank
    when(context.getParameters()).thenReturn(Map.of(
      "targetPlayerId", "   ",
      "hamsterType", "hamster_ninja"
    ));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> definition.execute(context));
    assertEquals("TWO_HAMSTERS: missing required parameter: targetPlayerId", exception.getMessage());
  }
}

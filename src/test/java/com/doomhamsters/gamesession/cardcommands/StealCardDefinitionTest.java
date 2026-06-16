package com.doomhamsters.gamesession.cardcommands;
import com.doomhamsters.Card;
import com.doomhamsters.Player;
import com.doomhamsters.Game;
import com.doomhamsters.gamesession.cardcommands.cards.StealCardDefinition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

class StealCardDefinitionTest {
  private StealCardDefinition stealCardDefinition;
  private CardCommandContext mockContext;
  private Game mockGame;
  private Player thief;
  private Player victim;

  @BeforeEach
  void setUp() {
    stealCardDefinition = new StealCardDefinition();
    mockContext = mock(CardCommandContext.class);
    mockGame = mock(Game.class);

    // 1. ECHTE Spieler erstellen (damit die Logik in Player.java funktioniert)
    thief = new Player("thief-id", "Christian");
    victim = new Player("victim-id", "Opfer");

    // 2. ECHTE Karte erstellen (Da victim echt ist, klappt removeFromHand problemlos!)
    Card testCard = new Card("card-1", "Golden Snack", "Snack");
    victim.addToHand(testCard);

    when(mockGame.getPlayers()).thenReturn(List.of(thief, victim));
    when(mockContext.getPlayer()).thenReturn(thief);
    when(mockContext.getGame()).thenReturn(mockGame);
  }

  @Test
  void execute_successfulSteal_transfersCard() {
    when(mockContext.getParameters()).thenReturn(Map.of("targetPlayerId", "victim-id"));

    CardCommandResult result = stealCardDefinition.execute(mockContext);
    assertTrue(victim.getHand().isEmpty(), "Das Opfer sollte keine Karten mehr haben.");

    assertEquals(1, thief.getHand().size(), "Der Dieb sollte jetzt genau eine Karte haben.");
    assertEquals("Golden Snack", thief.getHand().get(0).getName(), "Die geklaute Karte sollte der 'Golden Snack' sein.");
    assertNotNull(result.getPrivateMessage());
    assertTrue(result.getPublicMessage().contains("stole a card"));
  }
  @Test
  void execute_victimHasNoCards_returnsPublicMessageOnly() {
    when(mockContext.getParameters()).thenReturn(Map.of("targetPlayerId", "victim-id"));
    victim.removeFromHand("card-1");
    CardCommandResult result = stealCardDefinition.execute(mockContext);
    assertEquals(0, thief.getHand().size(), "Der Dieb sollte keine Karte bekommen haben.");
    assertNull(result.getPrivateMessage(), "Es sollte keine private Nachricht geben.");
    assertTrue(result.getPublicMessage().contains("had no cards"), "Die Nachricht sollte erwähnen, dass das Opfer keine Karten hat.");
  }
  @Test
  void execute_missingTargetPlayerId_throwsException() {
    when(mockContext.getParameters()).thenReturn(Map.of());
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      stealCardDefinition.execute(mockContext);
    });
    assertEquals("Target player ID is missing.", exception.getMessage());
  }
  @Test
  void simpleMethods_returnExpectedValues() {
    assertEquals("StealCard", stealCardDefinition.cardType(), "cardType sollte 'StealCard' sein.");
    assertEquals("STEAL_CARD", stealCardDefinition.commandId(), "commandId sollte 'STEAL_CARD' sein.");
    assertEquals("Steal Card", stealCardDefinition.displayName(), "displayName sollte 'Steal Card' sein.");
  }

  @Test
  void createTestingCard_returnsCorrectlyConfiguredCard() {
    Card testCard = stealCardDefinition.createTestingCard("player-99");

    assertEquals("steal_card_player-99", testCard.getId(), "Die ID sollte die Spieler-ID enthalten.");
    assertEquals("Steal Card", testCard.getName(), "Der Name sollte passen.");
    assertEquals("StealCard", testCard.getType(), "Der Typ sollte passen.");
  }

  @Test
  void execute_targetPlayerNotFound_returnsPublicMessage() {
    when(mockContext.getParameters()).thenReturn(Map.of("targetPlayerId", "ghost-player"));

    CardCommandResult result = stealCardDefinition.execute(mockContext);

    assertEquals(0, thief.getHand().size(), "Der Dieb sollte nichts bekommen haben.");
    assertNull(result.getPrivateMessage(), "Es sollte keine private Nachricht geben.");
    assertTrue(result.getPublicMessage().contains("the target had no cards!"),
      "Die Nachricht sollte den Fallback-Namen 'the target' verwenden.");
  }
}

package com.doomhamsters.gamesession.cardcommands;
import com.doomhamsters.Card;
import com.doomhamsters.Player;
import com.doomhamsters.Game;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
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

    thief = mock(Player.class);
    when(thief.getId()).thenReturn("thief-id");
    when(thief.getName()).thenReturn("Christian");

    List<Card> thiefHand = new ArrayList<>();
    when(thief.getHand()).thenReturn(thiefHand);

    victim = mock(Player.class);
    when(victim.getId()).thenReturn("victim-id");
    when(victim.getName()).thenReturn("Opfer");

    List<Card> victimHand = new ArrayList<>();
    victimHand.add(new Card("card-1", "Golden Snack", "Snack"));
    when(victim.getHand()).thenReturn(victimHand);

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
    victim.getHand().clear();
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
}

package com.doomhamsters.gamesession.cardcommands;
import com.doomhamsters.gamesession.cardcommands.cards.CageSwapCardDefinition;
import com.doomhamsters.Board;
import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
public class CageSwapCardDefinitionTest {
  private CageSwapCardDefinition cageSwapDefinition;
  private CardCommandContext context;
  private Game game;
  private Board board;
  private Player currentPlayer;
  private Player nextPlayer;

  @BeforeEach
  void setUp() {
    cageSwapDefinition = new CageSwapCardDefinition();

    context = mock(CardCommandContext.class);
    game = mock(Game.class);
    board = mock(Board.class);

    currentPlayer = new Player("p1", "Alice");
    nextPlayer = new Player("p2", "Bob");

    when(context.getPlayer()).thenReturn(currentPlayer);
    when(context.getGame()).thenReturn(game);
    when(game.getBoard()).thenReturn(board);

    when(board.getActivePlayers()).thenReturn(Arrays.asList(currentPlayer, nextPlayer));
  }

  @Test
  void testSwapsHandsSuccessfullyWhenBothHaveCards() {
    currentPlayer.addToHand(new Card("card1", "Card One", "action"));
    currentPlayer.addToHand(new Card("card2", "Card Two", "action"));

    nextPlayer.addToHand(new Card("card3", "Card Three", "action"));

    CardCommandResult result = cageSwapDefinition.execute(context);

    assertEquals("Alice swapped hands with Bob!", result.getPublicMessage());
    assertNull(result.getPrivateMessage());

    assertEquals(1, currentPlayer.getHand().size());
    assertEquals("card3", currentPlayer.getHand().get(0).getId());

    assertEquals(2, nextPlayer.getHand().size());
    assertEquals("card1", nextPlayer.getHand().get(0).getId());
  }

  @Test
  void testSwapsHandsSuccessfullyWhenCurrentPlayerHandIsEmpty() {
    nextPlayer.addToHand(new Card("card1", "Card One", "action"));
    nextPlayer.addToHand(new Card("card2", "Card Two", "action"));

    CardCommandResult result = cageSwapDefinition.execute(context);

    assertEquals("Alice swapped hands with Bob!", result.getPublicMessage());
    assertEquals(2, currentPlayer.getHand().size());
    assertTrue(nextPlayer.getHand().isEmpty());
  }

  @Test
  void testSwapsHandsSuccessfullyWhenNextPlayerHandIsEmpty() {
    currentPlayer.addToHand(new Card("card1", "Card One", "action"));
    currentPlayer.addToHand(new Card("card2", "Card Two", "action"));

    CardCommandResult result = cageSwapDefinition.execute(context);

    assertEquals("Alice swapped hands with Bob!", result.getPublicMessage());
    assertTrue(currentPlayer.getHand().isEmpty());
    assertEquals(2, nextPlayer.getHand().size());
  }

  @Test
  void testFailsWhenNoOtherActivePlayers() {
    when(board.getActivePlayers()).thenReturn(Collections.singletonList(currentPlayer));

    CardCommandResult result = cageSwapDefinition.execute(context);
    assertEquals("Alice tried to use Cage Swap, but there is no one left to swap with!", result.getPublicMessage());
  }
  @Test
  void testBasicCardProperties() {
    assertEquals("CageSwap", cageSwapDefinition.cardType());
    assertEquals("CAGE_SWAP", cageSwapDefinition.commandId());
    assertEquals("Cage Swap", cageSwapDefinition.displayName());

    Card testCard = cageSwapDefinition.createTestingCard("player_123");
    assertNotNull(testCard);
    assertEquals("cage_swap_player_123", testCard.getId());
    assertEquals("Cage Swap", testCard.getName());
    assertEquals("CageSwap", testCard.getType());
  }
}

package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.AbstractCardDefinition;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Card definition for Cage Swap.
 * Swaps the current player's entire hand with the next active player.
 */
@Component
public class CageSwapCardDefinition extends AbstractCardDefinition {

  public CageSwapCardDefinition() {
    super("CageSwap", "CAGE_SWAP", "Cage Swap");
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    Player currentPlayer = context.getPlayer();
    Game game = context.getGame();
    Player nextPlayer = getNextActivePlayer(game, currentPlayer);

    if (nextPlayer == null || nextPlayer.getId().equals(currentPlayer.getId())) {
      return CardCommandResult.publicOnly(
        currentPlayer.getName() + " tried to use Cage Swap, but there is no one left to swap with!"
      );
    }

    List<Card> currentHandCopy = new ArrayList<>(currentPlayer.getHand());
    List<Card> nextHandCopy = new ArrayList<>(nextPlayer.getHand());

    for (Card card : currentHandCopy) {
      currentPlayer.removeFromHand(card.getId());
      nextPlayer.addToHand(card);
    }
    for (Card card : nextHandCopy) {
      nextPlayer.removeFromHand(card.getId());
      currentPlayer.addToHand(card);
    }

    return CardCommandResult.publicOnly(
      currentPlayer.getName() + " swapped hands with " + nextPlayer.getName() + "!"
    );
  }

  /**
   * Helper method to find the next player in the turn order,
   * based on the active (living) players.
   */
  private Player getNextActivePlayer(Game game, Player currentPlayer) {
    List<Player> activePlayers = game.getBoard().getActivePlayers();

    if (activePlayers.size() <= 1) {
      return null;
    }

    int currentIndex = -1;
    for (int i = 0; i < activePlayers.size(); i++) {
      if (activePlayers.get(i).getId().equals(currentPlayer.getId())) {
        currentIndex = i;
        break;
      }
    }

    if (currentIndex == -1) {
      return null;
    }
    int nextIndex = (currentIndex + 1) % activePlayers.size();
    return activePlayers.get(nextIndex);
  }
}

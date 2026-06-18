package com.doomhamsters.gamesession.cardcommands.cards;

import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.LastActionSnapshot;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import com.doomhamsters.gamesession.cardcommands.CardDefinition;
import org.springframework.stereotype.Component;

/**
 * Card definition for Squick.
 *
 * <p>Cancels the most recently executed card command by restoring the game state to the snapshot
 * taken just before that command ran. All changes from the cancelled command — hand mutations,
 * life changes, deck modifications — are fully reverted.
 *
 * <p>Constraints:
 * <ul>
 *   <li>Squick cannot cancel another Squick.
 *   <li>Only one level of undo is available; the snapshot is consumed on use.
 *   <li>The Squick card itself is removed from the restored state so the same effect
 *       cannot be cancelled indefinitely.
 * </ul>
 */
@Component
public class SquickCardDefinition implements CardDefinition {

  static final String COMMAND_ID = "SQUICK";

  /** Required by Spring component scanning. */
  public SquickCardDefinition() {}

  @Override
  public String cardType() {
    return "Squick";
  }

  @Override
  public String commandId() {
    return COMMAND_ID;
  }

  @Override
  public String displayName() {
    return "Squick";
  }

  @Override
  public boolean isUndoable() {
    return false;
  }

  @Override
  public Card createTestingCard(String playerId) {
    return new Card("squick_" + playerId, displayName(), cardType());
  }

  @Override
  public CardCommandResult execute(CardCommandContext context) {
    LastActionSnapshot snapshot = context.getGame().getBoard().getLastActionSnapshot();

    if (snapshot == null) {
      throw new IllegalStateException("Squick: there is no card effect to cancel.");
    }

    if (COMMAND_ID.equalsIgnoreCase(snapshot.getCommandId())) {
      throw new IllegalStateException("Squick: another Squick cannot be cancelled.");
    }

    // Prevent double-undo via a stale game reference held in the same request cycle.
    context.getGame().getBoard().clearLastAction();

    Game restored = snapshot.getGame();

    // Remove the Squick card from the player's hand in the restored state so they cannot
    // replay Squick to cancel the same effect again.
    String squickCardId = context.getCard().getId();
    restored.getPlayers().stream()
        .filter(p -> p.getId().equals(context.getPlayer().getId()))
        .findFirst()
        .ifPresent(p -> p.removeFromHand(squickCardId));

    context.getSession().setGame(restored);

    return CardCommandResult.publicOnly(
        context.getPlayer().getName() + " played Squick and cancelled the last card effect.");
  }
}

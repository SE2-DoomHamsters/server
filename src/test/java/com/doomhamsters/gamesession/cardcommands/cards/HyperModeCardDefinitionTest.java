package com.doomhamsters.gamesession.cardcommands.cards;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.doomhamsters.Board;
import com.doomhamsters.Card;
import com.doomhamsters.Deck;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.cards.HyperModeCardDefinition;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HyperModeCardDefinitionTest {

  @Test
  void playingHyperModeAddsOneExtraTurn() {
    Player player = new Player("p1", "Alice");

    Game game = new Game();
    game.setPlayers(List.of(player));
    game.setBoard(new Board(List.of(player), new Deck()));

    HyperModeCardDefinition definition =
        new HyperModeCardDefinition();

    CardCommandContext context =
        new CardCommandContext(
            null,
            game,
            player,
            new Card(
                "hyper_mode_1",
                "Hyper Mode",
                "HyperMode"),
            Map.of());

    definition.execute(context);

    assertEquals(
        1,
        game.getBoard().getExtraTurns());
  }

  @Test
  void playingHyperModeTwiceStacksExtraTurns() {
    Player player = new Player("p1", "Alice");

    Game game = new Game();
    game.setPlayers(List.of(player));
    game.setBoard(new Board(List.of(player), new Deck()));

    HyperModeCardDefinition definition =
        new HyperModeCardDefinition();

    CardCommandContext context =
        new CardCommandContext(
            null,
            game,
            player,
            new Card(
                "hyper_mode_1",
                "Hyper Mode",
                "HyperMode"),
            Map.of());

    definition.execute(context);
    definition.execute(context);

    assertEquals(
        2,
        game.getBoard().getExtraTurns());
  }
}
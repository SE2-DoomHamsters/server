package com.doomhamsters.gamesession.cardcommands.cards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.doomhamsters.Board;
import com.doomhamsters.Card;
import com.doomhamsters.Deck;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
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

    CardCommandResult result =
        definition.execute(context);

    assertNotNull(result);

    assertEquals(
        1,
        game.getBoard().getExtraTurns());

    assertEquals(
        "Alice activated Hyper Mode. The next player gains an extra turn.",
        result.getPublicMessage());
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

    CardCommandResult firstResult =
        definition.execute(context);

    CardCommandResult secondResult =
        definition.execute(context);

    assertNotNull(firstResult);
    assertNotNull(secondResult);

    assertEquals(
        2,
        game.getBoard().getExtraTurns());

    assertEquals(
        "Alice activated Hyper Mode. The next player gains an extra turn.",
        firstResult.getPublicMessage());

    assertEquals(
        "Alice activated Hyper Mode. The next player gains an extra turn.",
        secondResult.getPublicMessage());
  }
}
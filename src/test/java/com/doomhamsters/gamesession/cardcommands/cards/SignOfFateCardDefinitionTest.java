package com.doomhamsters.gamesession.cardcommands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.doomhamsters.Card;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.cardcommands.cards.SignOfFateCardDefinition;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SignOfFateCardDefinitionTest {

  @Test
  void playingSignOfFateGivesPlayerOneLife() {
    Player player = new Player("p1", "Alice");

    int livesBefore = player.getLives();

    SignOfFateCardDefinition definition =
        new SignOfFateCardDefinition();

    CardCommandContext context =
        new CardCommandContext(
            null,
            null,
            player,
            new Card(
                "sign_of_fate_1",
                "Sign of Fate",
                "SignOfFate"),
            Map.of());

    definition.execute(context);

    assertEquals(
        livesBefore + 1,
        player.getLives());
  }
}
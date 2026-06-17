package com.doomhamsters.gamesession.cardcommands.cards;

import static org.junit.jupiter.api.Assertions.*;

import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HamsterTrioCardDefinitionTest {

  private HamsterTrioCardDefinition definition;

  private GameSession session;
  private Game game;

  private Player requester;
  private Player target;

  @BeforeEach
  void setUp() {
    definition = new HamsterTrioCardDefinition();

    session = new GameSession("game-1", "lobby-1");
    game = session.getGame();

    requester = new Player("p1", "Alice");
    target = new Player("p2", "Bob");

    game.setPlayers(
      java.util.List.of(
        requester,
        target
      )
    );
  }

  private CardCommandContext createContext(Map<String, Object> params) {
    return new CardCommandContext(
      session,
      game,
      requester,
      new Card("trigger", "Hamster Trio", "HamsterTrio"),
      params);
  }

  @Test
  void execute_throwsWhenTargetPlayerDoesNotExist() {

    requester.addToHand(new Card("h1", "Ninja", "hamster_ninja"));
    requester.addToHand(new Card("h2", "Ninja", "hamster_ninja"));
    requester.addToHand(new Card("h3", "Ninja", "hamster_ninja"));

    Map<String, Object> params = new HashMap<>();
    params.put("targetPlayerId", "unknown");
    params.put("cardType", "Beg");
    params.put("hamsterType", "hamster_ninja");

    IllegalArgumentException ex =
      assertThrows(
        IllegalArgumentException.class,
        () -> definition.execute(createContext(params)));

    assertTrue(ex.getMessage().contains("unknown targetPlayerId"));
  }

  @Test
  void execute_throwsWhenHamsterTypeDoesNotStartWithPrefix() {

    Map<String, Object> params = new HashMap<>();
    params.put("targetPlayerId", "p2");
    params.put("cardType", "Beg");
    params.put("hamsterType", "ninja");

    IllegalArgumentException ex =
      assertThrows(
        IllegalArgumentException.class,
        () -> definition.execute(createContext(params)));

    assertTrue(ex.getMessage().contains("must start with 'hamster_'"));
  }

  @Test
  void execute_throwsWhenRequiredParameterMissing() {

    Map<String, Object> params = new HashMap<>();
    params.put("targetPlayerId", "p2");
    params.put("cardType", "Beg");

    IllegalArgumentException ex =
      assertThrows(
        IllegalArgumentException.class,
        () -> definition.execute(createContext(params)));

    assertTrue(ex.getMessage().contains("missing required parameter"));
  }
  @Test
  void metadataIsCorrect() {
    HamsterTrioCardDefinition card = new HamsterTrioCardDefinition();

    assertEquals("HamsterTrio", card.cardType());
    assertEquals("HAMSTER_TRIO", card.commandId());
    assertEquals("Hamster Trio", card.displayName());
  }
  @Test
  void returnsMessageWhenTargetHasNoMatchingCard() {
    requester.addToHand(new Card("h1", "", "hamster_ninja"));
    requester.addToHand(new Card("h2", "", "hamster_ninja"));
    requester.addToHand(new Card("h3", "", "hamster_ninja"));

    CardCommandContext context =
      createContext(
        Map.of(
          "targetPlayerId", target.getId(),
          "cardType", "beg_for_snacks",
          "hamsterType", "hamster_ninja"));

    CardCommandResult result = definition.execute(context);

    assertEquals(0, requester.getHand().size());
    assertTrue(result.getPublicMessage().contains("had none"));
  }
  @Test
  void throwsWhenRequesterHasTooFewMatchingHamsters() {
    requester.addToHand(new Card("h1", "", "hamster_ninja"));
    requester.addToHand(new Card("h2", "", "hamster_ninja"));

    CardCommandContext context =
      createContext(
        Map.of(
          "targetPlayerId", target.getId(),
          "cardType", "beg_for_snacks",
          "hamsterType", "hamster_ninja"));

    assertThrows(
      IllegalStateException.class,
      () -> definition.execute(context));
  }
  @Test
  void throwsForUnknownTargetPlayer() {
    CardCommandContext context =
      createContext(
        Map.of(
          "targetPlayerId", "missing",
          "cardType", "beg_for_snacks",
          "hamsterType", "hamster_ninja"));

    assertThrows(
      IllegalArgumentException.class,
      () -> definition.execute(context));
  }
}

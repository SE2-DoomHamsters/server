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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

  @Test
  void execute_throwsWhenTargetIsSelf() {
    Map<String, Object> params = new HashMap<>();
    params.put("targetPlayerId", "p1");
    params.put("cardType", "beg_for_snacks");
    params.put("hamsterType", "hamster_ninja");

    IllegalArgumentException ex = assertThrows(
      IllegalArgumentException.class,
      () -> definition.execute(createContext(params)));

    assertTrue(ex.getMessage().contains("cannot target yourself"));
  }

  @Test
  void execute_throwsWhenTargetIsEliminated() {
    requester.addToHand(new Card("h1", "Ninja", "hamster_ninja"));
    requester.addToHand(new Card("h2", "Ninja", "hamster_ninja"));
    requester.addToHand(new Card("h3", "Ninja", "hamster_ninja"));

    // setPlayers() deep-copies, so decrement lives on the game's copy
    Player gameTarget = game.getPlayers().stream()
        .filter(p -> p.getId().equals(target.getId()))
        .findFirst().orElseThrow();
    gameTarget.decrementLives();
    gameTarget.decrementLives();
    gameTarget.decrementLives();

    Map<String, Object> params = new HashMap<>();
    params.put("targetPlayerId", "p2");
    params.put("cardType", "beg_for_snacks");
    params.put("hamsterType", "hamster_ninja");

    assertThrows(
      IllegalStateException.class,
      () -> definition.execute(createContext(params)));
  }

  @Test
  void stealsRequestedCardFromTarget() {
    requester.addToHand(new Card("h1", "Ninja", "hamster_ninja"));
    requester.addToHand(new Card("h2", "Ninja", "hamster_ninja"));
    requester.addToHand(new Card("h3", "Ninja", "hamster_ninja"));

    // setPlayers() deep-copies, so add the wanted card to the game's copy of target
    Player gameTarget = game.getPlayers().stream()
        .filter(p -> p.getId().equals(target.getId()))
        .findFirst().orElseThrow();
    Card wanted = new Card("c1", "Beg For Snacks", "beg_for_snacks");
    gameTarget.addToHand(wanted);

    CardCommandResult result = definition.execute(
      createContext(Map.of(
        "targetPlayerId", target.getId(),
        "cardType", "beg_for_snacks",
        "hamsterType", "hamster_ninja")));

    assertTrue(requester.getHand().contains(wanted));
    assertTrue(gameTarget.getHand().isEmpty());
    assertEquals(1, requester.getHand().size());
    assertTrue(result.getPublicMessage().contains("took"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"hamster_ninja", "hamster_viking", "hamster_wizard", "hamster_knight", "hamster_pirate"})
  void stealsRequestedCardForAllHamsterTypes(String hamsterType) {
    requester.addToHand(new Card("h1", "Hamster", hamsterType));
    requester.addToHand(new Card("h2", "Hamster", hamsterType));
    requester.addToHand(new Card("h3", "Hamster", hamsterType));

    // setPlayers() deep-copies, so add the wanted card to the game's copy of target
    Player gameTarget = game.getPlayers().stream()
        .filter(p -> p.getId().equals(target.getId()))
        .findFirst().orElseThrow();
    Card wanted = new Card("c1", "Beg For Snacks", "beg_for_snacks");
    gameTarget.addToHand(wanted);

    CardCommandResult result = definition.execute(
      createContext(Map.of(
        "targetPlayerId", target.getId(),
        "cardType", "beg_for_snacks",
        "hamsterType", hamsterType)));

    assertTrue(requester.getHand().contains(wanted));
    assertTrue(gameTarget.getHand().isEmpty());
    assertEquals(1, requester.getHand().size());
    assertTrue(result.getPublicMessage().contains("took"));
  }
}

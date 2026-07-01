package com.doomhamsters.gamesession.cardcommands.cards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.doomhamsters.Card;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import com.doomhamsters.gamesession.cardcommands.CardRegistry;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BegForSnacksCardDefinitionTest {

  private static final BegForSnacksCardDefinition DEFINITION = new BegForSnacksCardDefinition();

  private GameSession session;
  private Player requester;
  private Player target;
  private Card begCard;

  @BeforeEach
  void setUp() {
    session = new GameSession("game-1", "lobby-1");
    requester = new Player("p1", "Alice");
    target = new Player("p2", "Bob");
    session.getGame().setPlayers(List.of(requester, target));
    requester = session.getGame().getPlayers().get(0);
    target = session.getGame().getPlayers().get(1);
    begCard = new Card("bfs_p1", DEFINITION.displayName(), DEFINITION.cardType());

    requester.addToHand(begCard);
  }

  @Test
  void cardTypeMatchesExpected() {
    assertEquals("BegForSnacks", DEFINITION.cardType());
  }

  @Test
  void commandIdMatchesExpected() {
    assertEquals("BEG_FOR_SNACKS", DEFINITION.commandId());
  }

  @Test
  void effectIdDefaultsToCommandId() {
    assertEquals(DEFINITION.commandId(), DEFINITION.effectId());
  }

  @Test
  void registryResolvesFrontendWireCommandId() {
    CardRegistry registry = CardRegistry.of(DEFINITION);

    assertSame(DEFINITION, registry.get("BEG_FOR_SNACKS", "BegForSnacks"));
  }

  @Test
  void createTestingCardContainsPlayerId() {
    Card card = DEFINITION.createTestingCard("p1");

    assertTrue(card.getId().contains("p1"));
    assertEquals(DEFINITION.cardType(), card.getType());
  }

  @Test
  void missingTargetPlayerIdThrows() {
    CardCommandContext context = ctx(Map.of("cardType", "PowerNap"));

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> DEFINITION.execute(context));

    assertTrue(ex.getMessage().contains("targetPlayerId"));
  }

  @Test
  void missingCardTypeThrows() {
    CardCommandContext context = ctx(Map.of("targetPlayerId", "p2"));

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> DEFINITION.execute(context));

    assertTrue(ex.getMessage().contains("cardType"));
  }

  @Test
  void unknownTargetPlayerIdThrows() {
    CardCommandContext context = ctx(Map.of("targetPlayerId", "nobody", "cardType", "PowerNap"));

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> DEFINITION.execute(context));

    assertTrue(ex.getMessage().contains("unknown targetPlayerId"));
  }

  @Test
  void selfTargetPlayerIdThrows() {
    CardCommandContext context =
        ctx(Map.of("targetPlayerId", requester.getId(), "cardType", "PowerNap"));

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> DEFINITION.execute(context));

    assertTrue(ex.getMessage().contains("cannot target yourself"));
  }

  @Test
  void blankTargetPlayerIdThrows() {
    CardCommandContext context = ctx(Map.of("targetPlayerId", " ", "cardType", "PowerNap"));

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> DEFINITION.execute(context));

    assertTrue(ex.getMessage().contains("targetPlayerId"));
  }

  @Test
  void blankCardTypeThrows() {
    CardCommandContext context = ctx(Map.of("targetPlayerId", "p2", "cardType", " "));

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> DEFINITION.execute(context));

    assertTrue(ex.getMessage().contains("cardType"));
  }

  @Test
  void requestedCardIsTransferredFromTargetToRequester() {
    Card requestedCard = new Card("power_1", "Power Nap", "PowerNap");
    target.addToHand(requestedCard);
    requester.removeFromHand(begCard.getId());

    CardCommandResult result =
        DEFINITION.execute(ctx(Map.of("targetPlayerId", target.getId(), "cardType", "PowerNap")));

    assertTrue(requester.getHand().stream()
        .anyMatch(card -> requestedCard.getId().equals(card.getId())));
    assertFalse(target.getHand().stream()
        .anyMatch(card -> requestedCard.getId().equals(card.getId())));
    assertEquals("Alice begged Bob for a PowerNap and received one.", result.getPublicMessage());
    assertFalse(result.hasPrivateResult());
  }

  @Test
  void requestedCardTypeMatchIgnoresCase() {
    Card requestedCard = new Card("power_1", "Power Nap", "PowerNap");
    target.addToHand(requestedCard);
    requester.removeFromHand(begCard.getId());

    DEFINITION.execute(ctx(Map.of("targetPlayerId", target.getId(), "cardType", "powernap")));

    assertTrue(requester.getHand().stream()
        .anyMatch(card -> requestedCard.getId().equals(card.getId())));
    assertTrue(target.getHand().isEmpty());
  }

  @Test
  void onlyOneMatchingCardIsTransferred() {
    Card firstMatch = new Card("power_1", "Power Nap", "PowerNap");
    Card secondMatch = new Card("power_2", "Power Nap", "PowerNap");
    target.addToHand(firstMatch);
    target.addToHand(secondMatch);
    requester.removeFromHand(begCard.getId());

    DEFINITION.execute(ctx(Map.of("targetPlayerId", target.getId(), "cardType", "PowerNap")));

    assertEquals(1, requester.getHand().size());
    assertEquals(firstMatch.getId(), requester.getHand().getFirst().getId());
    assertEquals(1, target.getHand().size());
    assertEquals(secondMatch.getId(), target.getHand().getFirst().getId());
  }

  @Test
  void missingRequestedCardTransfersNothing() {
    requester.removeFromHand(begCard.getId());

    CardCommandResult result =
        DEFINITION.execute(ctx(Map.of("targetPlayerId", target.getId(), "cardType", "PowerNap")));

    assertTrue(requester.getHand().isEmpty());
    assertTrue(target.getHand().isEmpty());
    assertEquals("Alice begged Bob for a PowerNap - but they had none.", result.getPublicMessage());
    assertFalse(result.hasPrivateResult());
  }

  private CardCommandContext ctx(Map<String, Object> params) {
    return new CardCommandContext(session, session.getGame(), requester, begCard, params);
  }
}

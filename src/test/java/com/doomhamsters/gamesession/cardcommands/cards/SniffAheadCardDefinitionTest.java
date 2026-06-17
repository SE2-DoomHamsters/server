package com.doomhamsters.gamesession.cardcommands.cards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.doomhamsters.Card;
import com.doomhamsters.Deck;
import com.doomhamsters.gamesession.GameSession;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SniffAheadCardDefinitionTest {

  private SniffAheadCardDefinition definition;
  private GameSession session;

  @BeforeEach
  void setUp() {
    definition = new SniffAheadCardDefinition();
    session = new GameSession("test-1", "lobby-1");
    session.getGame().setup(
        List.of("Alice", "Bob"),
        Collections.emptyList(),
        new Card("ss_proto", "Snack Stash", "snack_stash"),
        List.of(new Card("doom_1", "Doom Hamster", "doom")));
  }

  @Test
  void returnsTopThreeCardsInDeckOrderWhenDeckHasMoreThanThree() {
    Card card1 = new Card("c1", "Card One", "action");
    Card card2 = new Card("c2", "Card Two", "action");
    Card card3 = new Card("c3", "Card Three", "action");
    Card card4 = new Card("c4", "Card Four", "action");
    session.getGame().setDeck(new Deck(List.of(card1, card2, card3, card4)));

    CardCommandResult result = execute();

    assertEquals(3, result.getRevealedCards().size());
    assertEquals("c1", result.getRevealedCards().get(0).getId());
    assertEquals("c2", result.getRevealedCards().get(1).getId());
    assertEquals("c3", result.getRevealedCards().get(2).getId());
    assertEquals(4, session.getGame().getDeck().size());
  }

  @Test
  void returnsAllAvailableCardsWhenDeckSmallerThanThree() {
    Card card1 = new Card("c1", "Card One", "action");
    Card card2 = new Card("c2", "Card Two", "action");
    session.getGame().setDeck(new Deck(List.of(card1, card2)));

    CardCommandResult result = execute();

    assertEquals(2, result.getRevealedCards().size());
    assertEquals("c1", result.getRevealedCards().get(0).getId());
    assertEquals("c2", result.getRevealedCards().get(1).getId());
    assertEquals(2, session.getGame().getDeck().size());
  }

  @Test
  void returnsEmptyListAndEmptyDeckMessageWhenDeckIsEmpty() {
    session.getGame().setDeck(new Deck(Collections.emptyList()));

    CardCommandResult result = execute();

    assertTrue(result.getRevealedCards().isEmpty());
    assertEquals("The deck is empty.", result.getPrivateMessage());
    assertTrue(result.hasPrivateResult());
  }

  @Test
  void privateMessageListsCardNamesAndDeckOrderIsUnchanged() {
    Card card1 = new Card("c1", "Alpha", "action");
    Card card2 = new Card("c2", "Beta", "action");
    Card card3 = new Card("c3", "Gamma", "action");
    session.getGame().setDeck(new Deck(List.of(card1, card2, card3)));

    CardCommandResult result = execute();

    assertEquals("Alice activated Sniff Ahead.", result.getPublicMessage());
    assertEquals("Top 3 card(s): Alpha, Beta, Gamma.", result.getPrivateMessage());

    List<String> deckIds = session.getGame().getDeck().getCards().stream()
        .map(Card::getId)
        .toList();
    assertEquals(List.of("c1", "c2", "c3"), deckIds);
  }

  private CardCommandResult execute() {
    Card sniffCard = new Card("sniff_1", "Sniff Ahead", "SniffAhead");
    return definition.execute(
        new CardCommandContext(
            session,
            session.getGame(),
            session.getGame().getPlayers().get(0),
            sniffCard,
            Collections.emptyMap()));
  }
}

package com.doomhamsters.gamesession.cardcommands;

import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import com.doomhamsters.gamesession.cardcommands.CardCommandContext;
import com.doomhamsters.gamesession.cardcommands.CardCommandResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

  class BegForSnacksCommandTest {

    private static final Card BEG_CARD = new Card("bfs_0", "Beg for Snacks", "beg_for_snacks");
    private static final Card POWER_NAP = new Card("pn_0", "Power Nap", "power_nap");

    private Game game;
    private GameSession session;
    private Player requester;
    private Player target;

    @BeforeEach
    void setUp() {
      requester = new Player("p1", "Alice");
      target    = new Player("p2", "Bob");
      requester.addToHand(BEG_CARD);

      game    = mock(Game.class);
      session = mock(GameSession.class);
      when(game.getPlayers()).thenReturn(List.of(requester, target));
    }

    private CardCommandContext ctx(Map<String, Object> params) {
      return new CardCommandContext(session, game, requester, BEG_CARD, params);
    }

    // ── happy paths ──────────────────────────────────────────────────

    @Test
    void targetHasCard_transfersExactlyOne() {
      Card copy1 = new Card("pn_0", "Power Nap", "power_nap");
      Card copy2 = new Card("pn_1", "Power Nap", "power_nap");
      target.addToHand(copy1);
      target.addToHand(copy2);

      CardCommandResult result = BegForSnacksCommand.INSTANCE.execute(
        ctx(Map.of("targetPlayerId", "p2", "cardType", "power_nap")));

      assertThat(target.getHand()).hasSize(1);
      assertThat(requester.getHand()).hasSize(1); // beg card removed, one power_nap added
      assertThat(result.getPublicMessage()).contains("received one");
    }

    @Test
    void targetHasCard_begCardRemovedFromRequester() {
      target.addToHand(new Card("pn_0", "Power Nap", "power_nap"));

      BegForSnacksCommand.INSTANCE.execute(
        ctx(Map.of("targetPlayerId", "p2", "cardType", "power_nap")));

      assertThat(requester.getHand())
        .noneMatch(c -> c.getId().equals("bfs_0"));
    }

    @Test
    void targetDoesNotHaveCard_nothingTransferred() {
      CardCommandResult result = BegForSnacksCommand.INSTANCE.execute(
        ctx(Map.of("targetPlayerId", "p2", "cardType", "power_nap")));

      assertThat(target.getHand()).isEmpty();
      assertThat(requester.getHand()).isEmpty(); // beg card still removed
      assertThat(result.getPublicMessage()).contains("had none");
    }

    @Test
    void cardTypeMatchIsCaseInsensitive() {
      target.addToHand(POWER_NAP);

      BegForSnacksCommand.INSTANCE.execute(
        ctx(Map.of("targetPlayerId", "p2", "cardType", "POWER_NAP")));

      assertThat(requester.getHand()).anyMatch(c -> c.getType().equals("power_nap"));
    }

    // ── validation ───────────────────────────────────────────────────

    @Test
    void missingTargetPlayerId_throws() {
      assertThatThrownBy(() -> BegForSnacksCommand.INSTANCE.execute(
        ctx(Map.of("cardType", "power_nap"))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("targetPlayerId");
    }

    @Test
    void missingCardType_throws() {
      assertThatThrownBy(() -> BegForSnacksCommand.INSTANCE.execute(
        ctx(Map.of("targetPlayerId", "p2"))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cardType");
    }

    @Test
    void unknownTargetId_throws() {
      assertThatThrownBy(() -> BegForSnacksCommand.INSTANCE.execute(
        ctx(Map.of("targetPlayerId", "nobody", "cardType", "power_nap"))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("unknown targetPlayerId");
    }

    @Test
    void targetingSelf_throws() {
      assertThatThrownBy(() -> BegForSnacksCommand.INSTANCE.execute(
        ctx(Map.of("targetPlayerId", "p1", "cardType", "power_nap"))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("yourself");
    }
  }


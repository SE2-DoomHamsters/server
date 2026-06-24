package com.doomhamsters.gamesession.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.doomhamsters.Card;
import com.doomhamsters.Deck;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import com.doomhamsters.gamesession.snackstash.SnackStashClaim;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GameStateMapperTest {

  @Test
  void shouldHideOtherPlayersHands() throws Exception {

    GameSession session =
        new GameSession("game-1", "lobby-1");

    Game game = session.getGame();
    game.setResolvingDoomPlayerId("p2");
    game.setPendingDoomCard(new Card("doom_pending", "Doom Hamster", "doom"));

    Player player1 = new Player("p1", "Alice");
    Player player2 = new Player("p2", "Bob");

    player1.addToHand(
        new Card("c1", "Attack", "action"));

    player2.addToHand(
        new Card("c2", "Defend", "action"));
    game.setPendingSnackStashClaim(
        new SnackStashClaim(
            "claim-1",
            "p2",
            "c2",
            123L,
            List.of("p1"),
            Map.of()));

    Field playersField =
        Game.class.getDeclaredField("players");
    Field deckField =
        Game.class.getDeclaredField("deck");

    playersField.setAccessible(true);
    deckField.setAccessible(true);

    List<Player> players = new ArrayList<>();
    players.add(player1);
    players.add(player2);

    playersField.set(game, players);
    deckField.set(game, new Deck(List.of(
        new Card("d1", "Draw 1", "action"),
        new Card("d2", "Draw 2", "action"),
        new Card("d3", "Draw 3", "action"))));

    GameStateMapper mapper =
        new GameStateMapper();

    GameStateDto dto =
        mapper.toFilteredDto(session, "p1");

    assertEquals(game.getResolvingDoomPlayerId(), dto.getResolvingDoomPlayerId());
    assertTrue(dto.isPendingDoomRequiresInsertion());
    assertEquals("doom_pending", dto.getPendingDoomCardId());
    assertEquals("claim-1", dto.getPendingSnackStashClaim().getClaimId());
    assertEquals("p2", dto.getPendingSnackStashClaim().getPlayerId());
    assertEquals("Bob", dto.getPendingSnackStashClaim().getPlayerName());
    assertEquals(1, dto.getPendingSnackStashClaim().getVotesRequired());
    assertEquals(0, dto.getPendingSnackStashClaim().getVotesReceived());
    assertEquals(3, dto.getRemainingDeckSize());

    assertEquals(2, dto.getPlayers().size());

    var ownPlayer =
        dto.getPlayers().stream()
            .filter(p -> p.getPlayerId().equals("p1"))
            .findFirst()
            .orElseThrow();

    var enemyPlayer =
        dto.getPlayers().stream()
            .filter(p -> p.getPlayerId().equals("p2"))
            .findFirst()
            .orElseThrow();

    assertEquals(1, ownPlayer.getHand().size());

    assertTrue(enemyPlayer.getHand().isEmpty());

    assertEquals(1, enemyPlayer.getHandSize());
  }
}

package com.doomhamsters.gamesession.snackstash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SnackStashClaimServiceTest {

  @Test
  void yesVotesAcceptClaimWithoutCheckingWhetherCardWasReallySnackStash() {
    GameSession session = runningSession(List.of("Alice", "Bob"));
    Game game = session.getGame();
    Player claimant = game.getPlayers().get(0);
    Player voter = game.getPlayers().get(1);
    Card fakeCard =
        claimant.getHand().stream()
            .filter(card -> !card.isSnackStash() && !card.isDoom())
            .findFirst()
            .orElseThrow();

    game.startDoomResolution(claimant, new Card("doom_drawn", "Doom", "doom"));

    SnackStashClaimService service = new SnackStashClaimService();
    SnackStashClaim claim =
        service.claimSnackStash(session, claimant.getId(), fakeCard.getId(), 1_000L);

    Optional<SnackStashResolution> resolution =
        service.vote(session, voter.getId(), claim.getClaimId(), SnackStashVote.YES);

    assertTrue(resolution.isPresent());
    assertEquals(SnackStashOutcome.UNCHALLENGED, resolution.orElseThrow().getOutcome());
    assertTrue(game.isPendingDoomRequiresInsertion());
    assertEquals("doom_drawn", game.getPendingDoomCardId());
    assertEquals(claimant.getId(), game.getResolvingDoomPlayerId());
    assertNull(game.getPendingSnackStashClaim());
    assertFalse(
        claimant.getHand().stream()
            .anyMatch(card -> fakeCard.getId().equals(card.getId())));
  }

  @Test
  void noVotesAgainstRealSnackStashCostEachAccuserOneLife() {
    GameSession session = runningSession(List.of("Alice", "Bob", "Cara"));
    Game game = session.getGame();
    Player claimant = game.getPlayers().get(0);
    Player firstVoter = game.getPlayers().get(1);
    Player secondVoter = game.getPlayers().get(2);
    Card snackStash =
        claimant.getHand().stream()
            .filter(Card::isSnackStash)
            .findFirst()
            .orElseThrow();
    int firstLivesBefore = firstVoter.getLives();
    int secondLivesBefore = secondVoter.getLives();

    game.startDoomResolution(claimant, new Card("doom_drawn", "Doom", "doom"));

    SnackStashClaimService service = new SnackStashClaimService();
    SnackStashClaim claim =
        service.claimSnackStash(session, claimant.getId(), snackStash.getId(), 1_000L);

    Optional<SnackStashResolution> waiting =
        service.vote(session, firstVoter.getId(), claim.getClaimId(), SnackStashVote.NO);
    Optional<SnackStashResolution> resolved =
        service.vote(session, secondVoter.getId(), claim.getClaimId(), SnackStashVote.NO);

    assertTrue(waiting.isEmpty());
    assertTrue(resolved.isPresent());
    SnackStashResolution resolution = resolved.orElseThrow();
    assertEquals(SnackStashOutcome.LEGITIMATE_CALL, resolution.getOutcome());
    assertEquals(List.of(firstVoter.getId(), secondVoter.getId()), resolution.getAccusingPlayerIds());
    assertEquals(2, resolution.getLifeChanges().size());
    assertEquals(firstLivesBefore - 1, firstVoter.getLives());
    assertEquals(secondLivesBefore - 1, secondVoter.getLives());
    assertTrue(game.isPendingDoomRequiresInsertion());
    assertNull(game.getPendingSnackStashClaim());
  }

  @Test
  void claimRejectsWhenNoDoomResolutionIsPending() {
    GameSession session = runningSession(List.of("Alice", "Bob"));
    Player claimant = session.getGame().getPlayers().get(0);
    Card snackStash = firstSnackStash(claimant);
    String claimantId = claimant.getId();
    String snackStashId = snackStash.getId();
    SnackStashClaimService service = new SnackStashClaimService();

    assertThrows(
        IllegalStateException.class,
        () -> service.claimSnackStash(session, claimantId, snackStashId, 1_000L));
  }

  @Test
  void claimRejectsWhenDifferentPlayerIsResolvingDoom() {
    GameSession session = runningSession(List.of("Alice", "Bob"));
    Game game = session.getGame();
    Player claimant = game.getPlayers().get(0);
    Player otherPlayer = game.getPlayers().get(1);
    Card otherSnackStash = firstSnackStash(otherPlayer);
    String otherPlayerId = otherPlayer.getId();
    String otherSnackStashId = otherSnackStash.getId();
    game.startDoomResolution(claimant, new Card("doom_drawn", "Doom", "doom"));
    SnackStashClaimService service = new SnackStashClaimService();

    assertThrows(
        IllegalArgumentException.class,
        () -> service.claimSnackStash(session, otherPlayerId, otherSnackStashId, 1_000L));
  }

  @Test
  void claimRejectsWhenNoDrawnDoomCardIsPending() {
    GameSession session = runningSession(List.of("Alice", "Bob"));
    Game game = session.getGame();
    Player claimant = game.getPlayers().get(0);
    Card snackStash = firstSnackStash(claimant);
    String claimantId = claimant.getId();
    String snackStashId = snackStash.getId();
    game.setResolvingDoomPlayerId(claimantId);
    SnackStashClaimService service = new SnackStashClaimService();

    assertThrows(
        IllegalStateException.class,
        () -> service.claimSnackStash(session, claimantId, snackStashId, 1_000L));
  }

  @Test
  void claimRejectsWhenAnotherClaimIsAlreadyPending() {
    GameSession session = runningSession(List.of("Alice", "Bob"));
    Game game = session.getGame();
    Player claimant = game.getPlayers().get(0);
    Card snackStash = firstSnackStash(claimant);
    String claimantId = claimant.getId();
    String snackStashId = snackStash.getId();
    game.startDoomResolution(claimant, new Card("doom_drawn", "Doom", "doom"));
    SnackStashClaimService service = new SnackStashClaimService();
    service.claimSnackStash(session, claimantId, snackStashId, 1_000L);

    assertThrows(
        IllegalStateException.class,
        () -> service.claimSnackStash(session, claimantId, snackStashId, 2_000L));
  }

  @Test
  void claimRejectsCardThatIsNotInHand() {
    GameSession session = runningSession(List.of("Alice", "Bob"));
    Game game = session.getGame();
    Player claimant = game.getPlayers().get(0);
    String claimantId = claimant.getId();
    game.startDoomResolution(claimant, new Card("doom_drawn", "Doom", "doom"));
    SnackStashClaimService service = new SnackStashClaimService();

    assertThrows(
        IllegalArgumentException.class,
        () -> service.claimSnackStash(session, claimantId, "missing-card", 1_000L));
  }

  @Test
  void voteRejectsWhenNoClaimIsPending() {
    GameSession session = runningSession(List.of("Alice", "Bob"));
    SnackStashClaimService service = new SnackStashClaimService();

    assertThrows(
        IllegalStateException.class,
        () -> service.vote(session, "p1", "claim-1", SnackStashVote.YES));
  }

  @Test
  void voteRejectsMismatchedClaimId() {
    GameSession session = runningSession(List.of("Alice", "Bob"));
    Game game = session.getGame();
    Player claimant = game.getPlayers().get(0);
    Player voter = game.getPlayers().get(1);
    Card snackStash = firstSnackStash(claimant);
    String claimantId = claimant.getId();
    String voterId = voter.getId();
    String snackStashId = snackStash.getId();
    game.startDoomResolution(claimant, new Card("doom_drawn", "Doom", "doom"));
    SnackStashClaimService service = new SnackStashClaimService();
    service.claimSnackStash(session, claimantId, snackStashId, 1_000L);

    assertThrows(
        IllegalArgumentException.class,
        () -> service.vote(session, voterId, "wrong-claim", SnackStashVote.YES));
  }

  @Test
  void voteRejectsClaimingPlayer() {
    GameSession session = runningSession(List.of("Alice", "Bob"));
    Game game = session.getGame();
    Player claimant = game.getPlayers().get(0);
    Card snackStash = firstSnackStash(claimant);
    String claimantId = claimant.getId();
    String snackStashId = snackStash.getId();
    game.startDoomResolution(claimant, new Card("doom_drawn", "Doom", "doom"));
    SnackStashClaimService service = new SnackStashClaimService();
    SnackStashClaim claim = service.claimSnackStash(session, claimantId, snackStashId, 1_000L);
    String claimId = claim.getClaimId();

    assertThrows(
        IllegalArgumentException.class,
        () -> service.vote(session, claimantId, claimId, SnackStashVote.YES));
  }

  @Test
  void voteRejectsIneligiblePlayer() {
    GameSession session = runningSession(List.of("Alice", "Bob", "Cara"));
    Game game = session.getGame();
    Player claimant = game.getPlayers().get(0);
    Player ineligiblePlayer = game.getPlayers().get(2);
    String claimantId = claimant.getId();
    String ineligiblePlayerId = ineligiblePlayer.getId();
    while (ineligiblePlayer.isAlive()) {
      ineligiblePlayer.decrementLives();
    }
    Card snackStash = firstSnackStash(claimant);
    String snackStashId = snackStash.getId();
    game.startDoomResolution(claimant, new Card("doom_drawn", "Doom", "doom"));
    SnackStashClaimService service = new SnackStashClaimService();
    SnackStashClaim claim = service.claimSnackStash(session, claimantId, snackStashId, 1_000L);
    String claimId = claim.getClaimId();

    assertThrows(
        IllegalArgumentException.class,
        () -> service.vote(session, ineligiblePlayerId, claimId, SnackStashVote.YES));
  }

  @Test
  void voteRejectsDuplicateVoteBeforeClaimIsResolved() {
    GameSession session = runningSession(List.of("Alice", "Bob", "Cara"));
    Game game = session.getGame();
    Player claimant = game.getPlayers().get(0);
    Player voter = game.getPlayers().get(1);
    Card snackStash = firstSnackStash(claimant);
    String claimantId = claimant.getId();
    String voterId = voter.getId();
    String snackStashId = snackStash.getId();
    game.startDoomResolution(claimant, new Card("doom_drawn", "Doom", "doom"));
    SnackStashClaimService service = new SnackStashClaimService();
    SnackStashClaim claim = service.claimSnackStash(session, claimantId, snackStashId, 1_000L);
    String claimId = claim.getClaimId();
    service.vote(session, voterId, claimId, SnackStashVote.YES);

    assertThrows(
        IllegalStateException.class,
        () -> service.vote(session, voterId, claimId, SnackStashVote.NO));
  }

  @Test
  void voteRejectsMissingVoterEvenWhenClaimListsVoterAsEligible() {
    GameSession session = runningSession(List.of("Alice", "Bob"));
    Game game = session.getGame();
    Player claimant = game.getPlayers().get(0);
    game.setPendingSnackStashClaim(
        new SnackStashClaim(
            "claim-1",
            claimant.getId(),
            firstSnackStash(claimant).getId(),
            1_000L,
            List.of("missing-player"),
            java.util.Map.of()));
    SnackStashClaimService service = new SnackStashClaimService();

    assertThrows(
        IllegalArgumentException.class,
        () -> service.vote(session, "missing-player", "claim-1", SnackStashVote.YES));
  }

  private static GameSession runningSession(List<String> players) {
    GameSession session = new GameSession("game", "lobby");
    session
        .getGame()
        .setup(
            players,
            actionCards(30),
            new Card("snack_proto", "Snack Stash", "snack_stash"),
            doomCards(6));
    return session;
  }

  private static Card firstSnackStash(Player player) {
    return player.getHand().stream()
        .filter(Card::isSnackStash)
        .findFirst()
        .orElseThrow();
  }

  private static List<Card> actionCards(int count) {
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      cards.add(new Card("action_" + i, "Action " + i, "action"));
    }
    return cards;
  }

  private static List<Card> doomCards(int count) {
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      cards.add(new Card("doom_" + i, "Doom " + i, "doom"));
    }
    return cards;
  }
}

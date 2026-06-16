package com.doomhamsters.gamesession.snackstash;

import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Applies authoritative Snack Stash claim and vote rules.
 */
@Service
public class SnackStashClaimService {

  /**
   * Creates the claim service.
   */
  public SnackStashClaimService() {
    // Required by Spring for component instantiation.
  }

  /**
   * Starts a pending Snack Stash claim for the current Doom-resolving player.
   *
   * @param session game session
   * @param playerId claiming player id
   * @param cardId selected hand card id
   * @param nowEpochMillis current time
   * @return created pending claim
   */
  public SnackStashClaim claimSnackStash(
      GameSession session,
      String playerId,
      String cardId,
      long nowEpochMillis) {

    Game game = session.getGame();
    assertResolvingDoomPlayer(game, playerId);
    assertDrawnDoomPending(game);
    if (game.getPendingSnackStashClaim() != null) {
      throw new IllegalStateException("A Snack Stash claim is already pending.");
    }

    Player player = findPlayer(game, playerId);
    findCard(player, cardId);

    SnackStashClaim claim =
        new SnackStashClaim(
            UUID.randomUUID().toString(),
            playerId,
            cardId,
            nowEpochMillis,
            eligibleVoterIds(game, playerId),
            Map.of());

    game.setPendingSnackStashClaim(claim);
    return claim;
  }

  /**
   * Records one vote and resolves the claim only when every eligible voter has voted.
   *
   * @param session game session
   * @param voterId voter player id
   * @param claimId expected claim id
   * @param vote submitted vote
   * @return resolution result once the final required vote arrives
   */
  public Optional<SnackStashResolution> vote(
      GameSession session,
      String voterId,
      String claimId,
      SnackStashVote vote) {

    Game game = session.getGame();
    SnackStashClaim claim = requireMatchingClaim(game, claimId);
    if (voterId.equals(claim.getPlayerId())) {
      throw new IllegalArgumentException("A player cannot vote on their own Snack Stash claim.");
    }
    if (!claim.getEligibleVoterIds().contains(voterId)) {
      throw new IllegalArgumentException("Player " + voterId + " is not eligible to vote.");
    }
    if (claim.hasVoteFrom(voterId)) {
      throw new IllegalStateException("Player " + voterId + " has already voted.");
    }

    findPlayer(game, voterId);
    claim.recordVote(voterId, vote);
    game.setPendingSnackStashClaim(claim);
    if (!claim.isFullyVoted()) {
      return Optional.empty();
    }

    return Optional.of(resolveCompletedVote(game, claim));
  }

  private SnackStashResolution resolveCompletedVote(
      Game game,
      SnackStashClaim claim) {

    Player claimant = findPlayer(game, claim.getPlayerId());
    Card claimedCard = findCard(claimant, claim.getCardId());
    List<String> accusingPlayerIds = accusingPlayerIds(claim);

    if (accusingPlayerIds.isEmpty()) {
      return resolveAcceptedClaim(game, claim, claimant);
    }

    if (claimedCard.isSnackStash()) {
      return resolveLegitimateClaimAfterNoVotes(game, claim, claimant, accusingPlayerIds);
    }

    return resolveCheater(game, claim, claimant, accusingPlayerIds);
  }

  private SnackStashResolution resolveAcceptedClaim(
      Game game,
      SnackStashClaim claim,
      Player claimant) {

    game.defusePendingDoomWithClaimedCard(claim.getCardId());

    return SnackStashResolution.builder(SnackStashOutcome.UNCHALLENGED, claim.getClaimId())
        .claimingPlayer(claimant.getId(), claimant.getName())
        .doomDefused(true)
        .message("Every voter accepted " + claimant.getName() + "'s Snack Stash claim.")
        .build();
  }

  private SnackStashResolution resolveLegitimateClaimAfterNoVotes(
      Game game,
      SnackStashClaim claim,
      Player claimant,
      List<String> accusingPlayerIds) {

    List<SnackStashLifeChange> lifeChanges = new ArrayList<>();
    for (String playerId : accusingPlayerIds) {
      Player voter = findPlayer(game, playerId);
      int livesBefore = voter.getLives();
      voter.decrementLives();
      lifeChanges.add(
          new SnackStashLifeChange(
              voter.getId(),
              voter.getName(),
              livesBefore,
              voter.getLives()));
    }
    game.defusePendingDoomWithClaimedCard(claim.getCardId());
    game.checkWinCondition();

    return SnackStashResolution.builder(SnackStashOutcome.LEGITIMATE_CALL, claim.getClaimId())
        .claimingPlayer(claimant.getId(), claimant.getName())
        .accusingPlayerIds(accusingPlayerIds)
        .lifeChanges(lifeChanges)
        .doomDefused(true)
        .message(claimant.getName() + " had a real Snack Stash. NO voters lose 1 life.")
        .build();
  }

  private SnackStashResolution resolveCheater(
      Game game,
      SnackStashClaim claim,
      Player claimant,
      List<String> accusingPlayerIds) {

    int livesBefore = claimant.getLives();
    game.acceptPendingDoomWithLifeLoss();

    return SnackStashResolution.builder(SnackStashOutcome.CHEATER, claim.getClaimId())
        .claimingPlayer(claimant.getId(), claimant.getName())
        .accusingPlayerIds(accusingPlayerIds)
        .lifeChanges(
            List.of(
                new SnackStashLifeChange(
                    claimant.getId(),
                    claimant.getName(),
                    livesBefore,
                    claimant.getLives())))
        .doomDefused(false)
        .message(claimant.getName() + " was caught cheating and loses 1 life.")
        .build();
  }

  private SnackStashClaim requireMatchingClaim(Game game, String claimId) {
    SnackStashClaim claim = game.getPendingSnackStashClaim();
    if (claim == null) {
      throw new IllegalStateException("No Snack Stash claim is pending.");
    }
    if (!claim.getClaimId().equals(claimId)) {
      throw new IllegalArgumentException("Snack Stash claim id does not match.");
    }
    return claim;
  }

  private void assertResolvingDoomPlayer(Game game, String playerId) {
    String resolvingPlayerId = game.getResolvingDoomPlayerId();
    if (resolvingPlayerId == null) {
      throw new IllegalStateException("No doom resolution is pending.");
    }
    if (!playerId.equals(resolvingPlayerId)) {
      throw new IllegalArgumentException("Player " + playerId + " is not resolving doom.");
    }
  }

  private void assertDrawnDoomPending(Game game) {
    if (!game.hasDrawnDoomPendingResolution()) {
      throw new IllegalStateException("No drawn Doom card is awaiting Snack Stash resolution.");
    }
  }

  private Player findPlayer(Game game, String playerId) {
    return game.getPlayers().stream()
        .filter(player -> player.getId().equals(playerId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
  }

  private Card findCard(Player player, String cardId) {
    return player.getHand().stream()
        .filter(card -> card.getId().equals(cardId))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException(
                "No card with id " + cardId + " in " + player.getName() + "'s hand"));
  }

  private List<String> eligibleVoterIds(Game game, String claimingPlayerId) {
    return game.getPlayers().stream()
        .filter(Player::isAlive)
        .map(Player::getId)
        .filter(playerId -> !playerId.equals(claimingPlayerId))
        .toList();
  }

  private List<String> accusingPlayerIds(SnackStashClaim claim) {
    return claim.getVotesByPlayerId().entrySet().stream()
        .filter(entry -> entry.getValue() == SnackStashVote.NO)
        .map(java.util.Map.Entry::getKey)
        .toList();
  }
}

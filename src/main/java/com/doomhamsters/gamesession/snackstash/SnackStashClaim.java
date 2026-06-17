package com.doomhamsters.gamesession.snackstash;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * Pending public claim that a selected hand card is being used as Snack Stash.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class SnackStashClaim {

  private final String claimId;
  private final String playerId;
  private final String cardId;
  private final long createdAtEpochMillis;
  private final List<String> eligibleVoterIds;
  private final Map<String, SnackStashVote> votesByPlayerId;

  /**
   * Creates a claim from persisted JSON.
   *
   * @param claimId claim identifier
   * @param playerId claiming player identifier
   * @param cardId selected card identifier
   * @param createdAtEpochMillis creation time
   * @param eligibleVoterIds players who must vote before resolution
   * @param votesByPlayerId votes already submitted by player id
   */
  @JsonCreator
  public SnackStashClaim(
      @JsonProperty("claimId") String claimId,
      @JsonProperty("playerId") String playerId,
      @JsonProperty("cardId") String cardId,
      @JsonProperty("createdAtEpochMillis") long createdAtEpochMillis,
      @JsonProperty("eligibleVoterIds") List<String> eligibleVoterIds,
      @JsonProperty("votesByPlayerId") Map<String, SnackStashVote> votesByPlayerId) {

    this.claimId = claimId;
    this.playerId = playerId;
    this.cardId = cardId;
    this.createdAtEpochMillis = createdAtEpochMillis;
    this.eligibleVoterIds =
        eligibleVoterIds == null ? new ArrayList<>() : new ArrayList<>(eligibleVoterIds);
    this.votesByPlayerId =
        votesByPlayerId == null ? new LinkedHashMap<>() : new LinkedHashMap<>(votesByPlayerId);
  }

  /**
   * Creates a defensive copy.
   *
   * @param other source claim
   */
  public SnackStashClaim(SnackStashClaim other) {
    this(
        other.getClaimId(),
        other.getPlayerId(),
        other.getCardId(),
        other.getCreatedAtEpochMillis(),
        other.getEligibleVoterIds(),
        other.getVotesByPlayerId());
  }

  /**
   * Returns the ids of players allowed to vote.
   *
   * @return defensive copy of eligible voter ids
   */
  public List<String> getEligibleVoterIds() {
    return List.copyOf(eligibleVoterIds);
  }

  /**
   * Returns submitted votes keyed by player id.
   *
   * @return defensive copy of submitted votes
   */
  public Map<String, SnackStashVote> getVotesByPlayerId() {
    return Collections.unmodifiableMap(new LinkedHashMap<>(votesByPlayerId));
  }

  /**
   * Returns the number of votes required to resolve the claim.
   *
   * @return required vote count
   */
  public int getVotesRequired() {
    return eligibleVoterIds.size();
  }

  /**
   * Returns the number of submitted votes.
   *
   * @return submitted vote count
   */
  public int getVotesReceived() {
    return votesByPlayerId.size();
  }

  /**
   * Returns whether a player has already voted.
   *
   * @param playerId player id to check
   * @return {@code true} if the player has submitted a vote
   */
  public boolean hasVoteFrom(String playerId) {
    return votesByPlayerId.containsKey(playerId);
  }

  /**
   * Records or replaces a player's vote.
   *
   * @param playerId voting player id
   * @param vote submitted vote
   */
  public void recordVote(String playerId, SnackStashVote vote) {
    votesByPlayerId.put(playerId, vote);
  }

  /**
   * Returns whether all required voters have voted.
   *
   * @return {@code true} once the claim can resolve
   */
  public boolean isFullyVoted() {
    return getVotesReceived() >= getVotesRequired();
  }
}

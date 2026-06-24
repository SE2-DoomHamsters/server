package com.doomhamsters;

import com.doomhamsters.gamesession.snackstash.SnackStashClaim;

final class DoomResolutionState {

  private String resolvingPlayerId;
  private Card drawnDoomCard;
  private Card pendingDoomCard;
  private SnackStashClaim pendingSnackStashClaim;

  DoomResolutionState() {
  }

  DoomResolutionState(DoomResolutionState other) {
    this.resolvingPlayerId = other.resolvingPlayerId;
    this.drawnDoomCard =
        other.drawnDoomCard == null ? null : new Card(other.drawnDoomCard);
    this.pendingDoomCard =
        other.pendingDoomCard == null ? null : new Card(other.pendingDoomCard);
    this.pendingSnackStashClaim =
        other.pendingSnackStashClaim == null
            ? null
            : new SnackStashClaim(other.pendingSnackStashClaim);
  }

  String getResolvingPlayerId() {
    return resolvingPlayerId;
  }

  void setResolvingPlayerId(String resolvingPlayerId) {
    this.resolvingPlayerId = resolvingPlayerId;
  }

  void clearResolvingPlayerId() {
    resolvingPlayerId = null;
  }

  boolean hasDrawnDoomCard() {
    return drawnDoomCard != null;
  }

  Card getDrawnDoomCard() {
    return drawnDoomCard == null ? null : new Card(drawnDoomCard);
  }

  void setDrawnDoomCard(Card drawnDoomCard) {
    this.drawnDoomCard = drawnDoomCard == null ? null : new Card(drawnDoomCard);
  }

  void clearDrawnDoomCard() {
    drawnDoomCard = null;
  }

  boolean hasPendingDoomCard() {
    return pendingDoomCard != null;
  }

  Card getPendingDoomCard() {
    return pendingDoomCard == null ? null : new Card(pendingDoomCard);
  }

  void setPendingDoomCard(Card pendingDoomCard) {
    this.pendingDoomCard = pendingDoomCard == null ? null : new Card(pendingDoomCard);
  }

  void clearPendingDoomCard() {
    pendingDoomCard = null;
  }

  SnackStashClaim getPendingSnackStashClaim() {
    return pendingSnackStashClaim == null ? null : new SnackStashClaim(pendingSnackStashClaim);
  }

  void setPendingSnackStashClaim(SnackStashClaim pendingSnackStashClaim) {
    this.pendingSnackStashClaim =
        pendingSnackStashClaim == null
            ? null
            : new SnackStashClaim(pendingSnackStashClaim);
  }

  void clearPendingSnackStashClaim() {
    pendingSnackStashClaim = null;
  }

  void startDoomResolution(Player player, Card doomCard) {
    resolvingPlayerId = player.getId();
    setDrawnDoomCard(doomCard);
    clearPendingDoomCard();
    clearPendingSnackStashClaim();
  }

  void moveDrawnDoomToPendingInsertion(Card doomCard) {
    setPendingDoomCard(doomCard);
    clearDrawnDoomCard();
    clearPendingSnackStashClaim();
  }

  void clearDoomResolution() {
    clearDrawnDoomCard();
    clearResolvingPlayerId();
    clearPendingSnackStashClaim();
  }

  void clearAll() {
    clearResolvingPlayerId();
    clearDrawnDoomCard();
    clearPendingDoomCard();
    clearPendingSnackStashClaim();
  }
}

import java.util.List;

public class GameState {
  /**
   * Snapshot of the full game at a point in time.
   *
   * <p>Broadcast to {@code /topic/game/{gameId}} after every state change.
   */
  public final class GameStatee {

    private final String gameId;
    private final List<Player> players;
    private final String currentTurnPlayerId;

    /** Indicates a Snack Stash card is pending client confirmation for {@code pendingPlayerId}. */
    private final boolean snackStashPending;
    private final String pendingPlayerId;

    public GameStatee(
      String gameId,
      List<Player> players,
      String currentTurnPlayerId,
      boolean snackStashPending,
      String pendingPlayerId) {
      this.gameId = gameId;
      this.players = List.copyOf(players);
      this.currentTurnPlayerId = currentTurnPlayerId;
      this.snackStashPending = snackStashPending;
      this.pendingPlayerId = pendingPlayerId;
    }

    public String getGameId() { return gameId; }
    public List<Player> getPlayers() { return players; }
    public String getCurrentTurnPlayerId() { return currentTurnPlayerId; }
    public boolean isSnackStashPending() { return snackStashPending; }
    public String getPendingPlayerId() { return pendingPlayerId; }
  }
}

public final class DrawError {
  /** Error response sent to the requesting client on a failed draw attempt. */


    private final String gameId;
    private final String playerId;
    private final String reason;

    public DrawError(String gameId, String playerId, String reason) {
      this.gameId = gameId;
      this.playerId = playerId;
      this.reason = reason;
    }

    public String getGameId() { return gameId; }
    public String getPlayerId() { return playerId; }
    public String getReason() { return reason; }
  }


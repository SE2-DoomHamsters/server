public final class DrawRequest {
  /** Payload sent by the client when requesting to draw a card. */

    private String playerId;

    public DrawRequest() {}

    public DrawRequest(String playerId) {
      this.playerId = playerId;
    }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
  }


/** Payload sent by the client when requesting to draw a card. */
public final class DrawRequest {

  private String playerId;

  /**DrawRequest empty Constructor. */
  public DrawRequest() {}

  /**DrawRequest Constructor for playerId.*/
  public DrawRequest(String playerId) {
    this.playerId = playerId;
  }

  public String getPlayerId() {
    return playerId;
  }

  public void setPlayerId(String playerId) {
    this.playerId = playerId;
  }

}


package com.doomhamsters.gamesession.cardcommands;

import java.util.Collections;
import java.util.Map;

/**
 * Payload sent by clients to activate a card command.
 */
public class ActivateCardCommandRequest {

  private String playerId;
  private String cardId;
  private String cardType;
  private String commandId;
  private Map<String, Object> parameters = Collections.emptyMap();

  public String getPlayerId() {
    return playerId;
  }

  public void setPlayerId(String playerId) {
    this.playerId = playerId;
  }

  public String getCardId() {
    return cardId;
  }

  public void setCardId(String cardId) {
    this.cardId = cardId;
  }

  public String getCardType() {
    return cardType;
  }

  public void setCardType(String cardType) {
    this.cardType = cardType;
  }

  public String getCommandId() {
    return commandId;
  }

  public void setCommandId(String commandId) {
    this.commandId = commandId;
  }

  /**
   * Returns command parameters supplied by the client.
   *
   * @return immutable command parameters
   */
  public Map<String, Object> getParameters() {
    return parameters == null
        ? Collections.emptyMap()
        : Collections.unmodifiableMap(parameters);
  }

  /**
   * Sets command parameters supplied by the client.
   *
   * @param parameters command parameters
   */
  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters == null
        ? Collections.emptyMap()
        : Map.copyOf(parameters);
  }
}

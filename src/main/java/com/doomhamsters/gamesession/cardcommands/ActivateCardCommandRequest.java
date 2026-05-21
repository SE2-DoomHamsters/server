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

  /**
   * Creates an empty card command request.
   */
  public ActivateCardCommandRequest() {
    // Required by Jackson when binding STOMP command payloads.
  }

  /**
   * Returns the player activating the card.
   *
   * @return player id
   */
  public String getPlayerId() {
    return playerId;
  }

  /**
   * Sets the player activating the card.
   *
   * @param playerId player id
   */
  public void setPlayerId(String playerId) {
    this.playerId = playerId;
  }

  /**
   * Returns the activated card id.
   *
   * @return card id
   */
  public String getCardId() {
    return cardId;
  }

  /**
   * Sets the activated card id.
   *
   * @param cardId card id
   */
  public void setCardId(String cardId) {
    this.cardId = cardId;
  }

  /**
   * Returns the activated card type.
   *
   * @return card type
   */
  public String getCardType() {
    return cardType;
  }

  /**
   * Sets the activated card type.
   *
   * @param cardType card type
   */
  public void setCardType(String cardType) {
    this.cardType = cardType;
  }

  /**
   * Returns the command requested by the client.
   *
   * @return command id
   */
  public String getCommandId() {
    return commandId;
  }

  /**
   * Sets the command requested by the client.
   *
   * @param commandId command id
   */
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

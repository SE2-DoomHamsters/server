package com.doomhamsters.gamesession.cardcommands;

import com.doomhamsters.gamesession.dto.CardDto;

/**
 * Public event emitted when a visible card command is played.
 */
public class CardCommandPlayedEventDto {

  private static final String TYPE = "CARD_COMMAND_PLAYED";

  private String playerId;
  private String playerName;
  private String commandId;
  private CardDto card;
  private String message;

  /**
   * Creates an empty card-played event.
   */
  public CardCommandPlayedEventDto() {
    // Required by Jackson for STOMP message serialization.
  }

  /**
   * Returns the event type.
   *
   * @return event type constant
   */
  public String getType() {
    return TYPE;
  }

  /**
   * Returns the player who played the command card.
   *
   * @return player id
   */
  public String getPlayerId() {
    return playerId;
  }

  /**
   * Sets the player who played the command card.
   *
   * @param playerId player id
   */
  public void setPlayerId(String playerId) {
    this.playerId = playerId;
  }

  /**
   * Returns the player display name.
   *
   * @return player display name
   */
  public String getPlayerName() {
    return playerName;
  }

  /**
   * Sets the player display name.
   *
   * @param playerName player display name
   */
  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  /**
   * Returns the command id.
   *
   * @return command id
   */
  public String getCommandId() {
    return commandId;
  }

  /**
   * Sets the command id.
   *
   * @param commandId command id
   */
  public void setCommandId(String commandId) {
    this.commandId = commandId;
  }

  /**
   * Returns the played card DTO.
   *
   * @return defensive card DTO copy, or {@code null}
   */
  public CardDto getCard() {
    return card == null ? null : new CardDto(card);
  }

  /**
   * Sets the played card DTO.
   *
   * @param card played card DTO
   */
  public void setCard(CardDto card) {
    this.card = card == null ? null : new CardDto(card);
  }

  /**
   * Returns the public event message.
   *
   * @return event message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the public event message.
   *
   * @param message event message
   */
  public void setMessage(String message) {
    this.message = message;
  }
}

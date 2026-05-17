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

  public String getType() {
    return TYPE;
  }

  public String getPlayerId() {
    return playerId;
  }

  public void setPlayerId(String playerId) {
    this.playerId = playerId;
  }

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public String getCommandId() {
    return commandId;
  }

  public void setCommandId(String commandId) {
    this.commandId = commandId;
  }

  public CardDto getCard() {
    return card == null ? null : new CardDto(card);
  }

  public void setCard(CardDto card) {
    this.card = card == null ? null : new CardDto(card);
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}

package com.doomhamsters.gamesession.cardcommands;

import com.doomhamsters.gamesession.dto.CardDto;

/**
 * Private event emitted to the player who activated a card command.
 */
public class CardCommandResultEventDto {

  private static final String TYPE = "CARD_COMMAND_RESULT";

  private String playerId;
  private String commandId;
  private CardDto card;
  private CardDto revealedCard;
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

  public CardDto getRevealedCard() {
    return revealedCard == null ? null : new CardDto(revealedCard);
  }

  public void setRevealedCard(CardDto revealedCard) {
    this.revealedCard = revealedCard == null ? null : new CardDto(revealedCard);
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}

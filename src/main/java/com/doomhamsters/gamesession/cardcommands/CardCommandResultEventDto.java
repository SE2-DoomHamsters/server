package com.doomhamsters.gamesession.cardcommands;

import com.doomhamsters.gamesession.dto.CardDto;
import java.util.List;

/**
 * Private event emitted to the player who activated a card command.
 */
public class CardCommandResultEventDto {

  private static final String TYPE = "CARD_COMMAND_RESULT";

  private String playerId;
  private String commandId;
  private CardDto card;
  private CardDto revealedCard;
  private List<CardDto> revealedCards;
  private String message;

  /**
   * Creates an empty card command result event.
   */
  public CardCommandResultEventDto() {
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
   * Returns the player receiving the private result.
   *
   * @return player id
   */
  public String getPlayerId() {
    return playerId;
  }

  /**
   * Sets the player receiving the private result.
   *
   * @param playerId player id
   */
  public void setPlayerId(String playerId) {
    this.playerId = playerId;
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
   * Returns the privately revealed card.
   *
   * @return defensive revealed card DTO copy, or {@code null}
   */
  public CardDto getRevealedCard() {
    return revealedCard == null ? null : new CardDto(revealedCard);
  }

  /**
   * Sets the privately revealed card.
   *
   * @param revealedCard revealed card DTO
   */
  public void setRevealedCard(CardDto revealedCard) {
    this.revealedCard = revealedCard == null ? null : new CardDto(revealedCard);
  }

  /**
   * Returns the privately revealed cards in deck order.
   *
   * @return defensive copies of revealed card DTOs, or an empty list
   */
  public List<CardDto> getRevealedCards() {
    if (revealedCards == null) {
      return List.of();
    }
    return revealedCards.stream().map(CardDto::new).toList();
  }

  /**
   * Sets the privately revealed cards in deck order.
   *
   * @param revealedCards revealed card DTOs
   */
  public void setRevealedCards(List<CardDto> revealedCards) {
    this.revealedCards = (revealedCards == null)
        ? List.of()
        : revealedCards.stream().map(CardDto::new).toList();
  }

  /**
   * Returns the private result message.
   *
   * @return result message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the private result message.
   *
   * @param message result message
   */
  public void setMessage(String message) {
    this.message = message;
  }
}

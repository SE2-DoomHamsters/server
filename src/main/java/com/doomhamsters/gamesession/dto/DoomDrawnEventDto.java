package com.doomhamsters.gamesession.dto;

/**
 * Private event sent to the player who drew a Doom Hamster card.
 */
public class DoomDrawnEventDto {

  private static final String TYPE = "DOOM_DRAWN";

  private CardDto card;

  /**
   * Creates an empty Doom-drawn event.
   */
  public DoomDrawnEventDto() {
  }

  /**
   * Creates a Doom-drawn event for the supplied card.
   *
   * @param card Doom card that was drawn
   */
  public DoomDrawnEventDto(CardDto card) {
    this.card = copyCard(card);
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
   * Returns the Doom card that was drawn.
   *
   * @return defensive card DTO copy
   */
  public CardDto getCard() {
    return copyCard(card);
  }

  /**
   * Sets the Doom card that was drawn.
   *
   * @param card Doom card DTO
   */
  public void setCard(CardDto card) {
    this.card = copyCard(card);
  }

  private CardDto copyCard(CardDto source) {
    if (source == null) {
      return null;
    }

    CardDto copy = new CardDto();

    copy.setId(source.getId());
    copy.setName(source.getName());
    copy.setType(source.getType());
    copy.setEffectId(source.getEffectId());

    return copy;
  }
}

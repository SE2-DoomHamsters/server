package com.doomhamsters.gamesession.dto;

/**
 * Private event sent to the player who drew a Doom Hamster card.
 */
public class DoomDrawnEventDto {

  private static final String TYPE = "DOOM_DRAWN";

  private CardDto card;

  public DoomDrawnEventDto() {
  }

  public DoomDrawnEventDto(CardDto card) {
    this.card = copyCard(card);
  }

  public String getType() {
    return TYPE;
  }

  public CardDto getCard() {
    return copyCard(card);
  }

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

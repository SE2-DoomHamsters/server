package com.doomhamsters.gamesession.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO representing a visible card.
 */
@Schema(description = "Visible card data returned to a player")
public class CardDto {

  @Schema(description = "Unique card instance identifier", example = "quick_peek-player-1")
  private String id;

  @Schema(description = "Card display name", example = "Quick Peek")
  private String name;

  @Schema(description = "Card type used by game logic", example = "QuickPeek")
  private String type;

  @Schema(description = "Playable effect identifier used by clients", example = "QUICK_PEEK")
  private String effectId;

  /**
   * Creates an empty card DTO.
   */
  public CardDto() {
  }

  /**
   * Creates a defensive copy of another card DTO.
   *
   * @param other card DTO to copy
   */
  public CardDto(CardDto other) {
    this.id = other.id;
    this.name = other.name;
    this.type = other.type;
    this.effectId = other.effectId;
  }

  /**
   * Returns the card instance id.
   *
   * @return card id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the card instance id.
   *
   * @param id card id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns the card display name.
   *
   * @return card name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the card display name.
   *
   * @param name card name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the card type.
   *
   * @return card type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the card type.
   *
   * @param type card type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Returns the playable effect id.
   *
   * @return effect id, or {@code null} for non-command cards
   */
  public String getEffectId() {
    return effectId;
  }

  /**
   * Sets the playable effect id.
   *
   * @param effectId effect id
   */
  public void setEffectId(String effectId) {
    this.effectId = effectId;
  }
}

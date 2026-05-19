package com.doomhamsters.gamesession.dto;

/**
 * DTO representing a visible card.
 */
public class CardDto {

  private String id;

  private String name;

  private String type;

  private String effectId;

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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getEffectId() {
    return effectId;
  }

  public void setEffectId(String effectId) {
    this.effectId = effectId;
  }
}

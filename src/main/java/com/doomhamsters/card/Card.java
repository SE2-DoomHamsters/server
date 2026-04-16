package com.doomhamsters.card;

public class Card {
  //Fields
  private final String name;
  private final CardType type;
  private final CardEffect effect;

  //Constructors
  public Card(String name, CardType type, CardEffect effect) {
    this.name = name;
    this.type = type;
    this.effect = effect;
  }

  //Getters & Setter
  public String getName() {
    return name;
  }
  public CardType getType() {
    return type;
  }
  public CardEffect getEffect() {return effect;}
}

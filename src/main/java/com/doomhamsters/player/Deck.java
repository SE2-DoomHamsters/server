package com.doomhamsters.player;

import com.doomhamsters.card.Card;
import com.doomhamsters.card.CardType;

import java.util.ArrayList;
import java.util.List;

public class Deck {
  //Fields
  private List<Card> cards = new ArrayList<>();

  //Constructors
  public Deck() {}
  public Deck(List<Card> cards) {cards.addAll(cards);}

  //Logic
  public void addCard(Card card) {cards.add(card);}
  public boolean hasSnackStash() {
    return cards.stream()
      .anyMatch(card -> card.getType() == CardType.SNACK_STASH);
  }

  //Getters & Setters
  public List<Card> getCards() { return cards; }
  public int getCurrentSize() { return cards.size(); }
}

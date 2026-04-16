package com.doomhamsters.player;

import com.doomhamsters.card.CardType;
import com.doomhamsters.utils.Rand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
  //Fields
  private final List<CardType> cards = new ArrayList<>();

  //Constructors
  public Deck() {}

  //Logic
  public void add(CardType card) {cards.add(card);}
  public void addCopies(CardType card, int amount){ for(int i = 0; i < amount; i++) add(card);}
  public CardType draw(){
    CardType card = Rand.pick(cards);
    cards.remove(card);
    return card;
  }
  public void shuffle(){Collections.shuffle(cards);}

  //Getters & Setters
  public List<CardType> getCards() { return cards; }
  public int getCurrentSize() { return cards.size(); }
}

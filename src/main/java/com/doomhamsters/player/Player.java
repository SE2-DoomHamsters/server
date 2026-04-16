package com.doomhamsters.player;

public class Player {
  //Fields
  private final String name;
  private int lives = 3;
  private final Deck deck;
  public boolean eliminated;

  //Constructors
  public Player(String name) {
    this.name = name;
    this.deck = new Deck();
    this.eliminated = false;
  }

  //Logic
  public boolean isAlive() { return lives > 0 && !eliminated;}

  //Getters & Setters
  public String getName() { return name; }
  public int getLives()   { return lives; }
  public Deck getDeck() { return deck; }
}

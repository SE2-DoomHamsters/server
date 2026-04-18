package com.doomhamsters.player;

import java.util.UUID;

public class Player {
  //Fields
  private final String id = UUID.randomUUID().toString();
  private final String token = UUID.randomUUID().toString();
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
  public boolean hasName(String name){return this.name.equals(name);}
  public boolean hasId(String id) { return this.id.equals(id); }

  //Getters & Setters
  public String getName() { return name; }
  public int getLives()   { return lives; }
  public Deck getDeck() { return deck; }
  public String getId() { return id; }
  public boolean hasToken(String token) { return this.token.equals(token); }
  public String getToken() { return token; }
}

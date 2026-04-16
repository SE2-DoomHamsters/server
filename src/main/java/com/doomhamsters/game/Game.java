package com.doomhamsters.game;

import com.doomhamsters.card.Card;
import com.doomhamsters.card.CardType;
import com.doomhamsters.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Game {
  //Fields
  private final String id;
  private static final int STARTING_HAND_SIZE = 6; // 1 Snack Stash + 5 zufällige
  private Player winner;
  private List<Player> players;
  private final List<Card> discardPile = new ArrayList<>();

  //Constructors
  public Game(String id){
    this.id = id;
  }

  //Logic
  public Player addPlayer(String playerName) {
    Player player = new Player(playerName);
    players.add(player);
    return player;
  }
  public void removePlayer(String playerName) {
    players.removeIf(p -> p.getName().equals(playerName));
  }
  public List<Player> getActivePlayers() {
    return players.stream()
      .filter(Player::isAlive)
      .toList();
  }
  public void start(String playerName){

  }
  public void playCard(String playerName, CardType type){

  }
  public void endTurn(String playerName){

  }
  public Card drawCard(String playerName){
    return null;
  }
  public void kickPlayer(String hostName, String targetPlayerName){

  }

  //Getters & Setters
  public Player getWinner() { return winner; }
  public List<Player> getPlayers() { return players; }
  public String getId() { return id; }
  public List<Card> getDiscardPile() {
    return discardPile;
  }

}

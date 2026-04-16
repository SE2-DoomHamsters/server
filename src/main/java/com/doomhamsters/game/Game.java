package com.doomhamsters.game;

import com.doomhamsters.card.CardType;
import com.doomhamsters.player.Deck;
import com.doomhamsters.player.Player;
import com.doomhamsters.utils.Rand;

import java.util.ArrayList;
import java.util.List;

public class Game {
  //Fields
  private GameState currentState = GameState.OFF;
  private final String id;
  private Player winner;
  private List<Player> players = new ArrayList<>();
  private Deck discardPile = new Deck(), drawPile = new Deck();
  private Player currentPlayer;

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

  public boolean start(){
    if(currentState == GameState.OFF) return false;
    currentState = GameState.STARTING;
    currentPlayer = Rand.pick(players);
    drawPile = DeckBuilder.buildBaseDeck(players.size());
    for(Player player : players) {
      player.getDeck().add(CardType.SNACK_STASH);
      for(int i = 0; i < 5; i++) player.getDeck().add(drawPile.draw());
    }
    DeckBuilder.addDoomCards(drawPile, players.size());
    currentState = GameState.RUNNING;
    return true;
  }
  public boolean playCard(String playerName, CardType type){
    if(!currentPlayer.hasName(playerName)) return false;
    return true;
  }
  public boolean endTurn(String playerName) {
    if (!currentPlayer.hasName(playerName)) return false;
    int nextIndex = (players.indexOf(currentPlayer) + 1) % players.size();
    currentPlayer = players.get(nextIndex);
    return true;
  }
  public CardType drawCard(String playerName){
    if(!currentPlayer.hasName(playerName)) return null;
    return drawPile.draw();
  }

  //Getters & Setters
  public Player getWinner() { return winner; }
  public List<Player> getPlayers() { return players; }
  public String getId() { return id; }
  public Deck getDiscardPile() {return discardPile;}
  public Player getCurrentPlayer() { return currentPlayer; }
  public GameState getCurrentState() { return currentState; }
}

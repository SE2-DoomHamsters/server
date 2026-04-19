package com.doomhamsters.game;

import com.doomhamsters.card.CardType;
import com.doomhamsters.player.Deck;
import com.doomhamsters.player.Player;
import com.doomhamsters.utils.Rand;
import java.util.ArrayList;
import java.util.List;

public class Game {

  private final String id;
  //Fields
  private GameState currentState = GameState.OFF;
  private Player winner, host;
  private final List<Player> players = new ArrayList<>();
  private final Deck discardPile = new Deck();
  private Deck drawPile = new Deck();
  private Player currentPlayer;

  //Constructors
  public Game(String id) {
    this.id = id;
  }

  //Logic
  public Player addPlayer(String playerName) {
    Player player = new Player(playerName);
    players.add(player);
    return player;
  }

  public void removePlayer(String token) {
    players.removeIf(p -> p.hasToken(token));
  }

  public boolean start() {
    if (currentState == GameState.OFF) {
      return false;
    }
    currentState = GameState.STARTING;
    currentPlayer = Rand.pick(players);
    drawPile = DeckBuilder.buildBaseDeck(players.size());
    for (Player player : players) {
      player.getDeck().add(CardType.SNACK_STASH);
      for (int i = 0; i < 5; i++) {
        player.getDeck().add(drawPile.draw());
      }
    }
    DeckBuilder.addDoomCards(drawPile, players.size());
    currentState = GameState.RUNNING;
    return true;
  }

  public boolean playCard(String token, CardType type) {
    return isCurrentPlayer(token);
  }

  public boolean endTurn(String token) {
    if (!isCurrentPlayer(token)) {
      return false;
    }
    int nextIndex = (players.indexOf(currentPlayer) + 1) % players.size();
    currentPlayer = players.get(nextIndex);
    return true;
  }

  public CardType drawCard(String token) {
    if (!isCurrentPlayer(token)) {
      return null;
    }
    return drawPile.draw();
  }

  //Getters & Setters
  public Player getWinner() {
    return winner;
  }

  public List<Player> getPlayers() {
    return players;
  }

  public String getId() {
    return id;
  }

  public Deck getDiscardPile() {
    return discardPile;
  }

  public Player getCurrentPlayer() {
    return currentPlayer;
  }

  public GameState getCurrentState() {
    return currentState;
  }

  public Player getHost() {
    return host;
  }

  public void setHost(Player host) {
    this.host = host;
  }

  public boolean isHost(String playerId) {
    return host.hasId(playerId);
  }

  public boolean isHostToken(String token) {
    return host.hasToken(token);
  }

  public Player getPlayerById(String playerId) {
    return players.stream().filter(p -> p.getName().equals(playerId)).findFirst().orElse(null);
  }

  public Player getPlayerByToken(String token) {
    return players.stream().filter(p -> p.hasToken(token)).findFirst().orElse(null);
  }

  public boolean isCurrentPlayer(String token) {
    return currentPlayer.hasToken(token);
  }
}

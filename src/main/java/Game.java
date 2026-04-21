import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
  public static final int STARTING_HAND_SIZE = 6; // 1 Snack Stash + 5 zufällige

  public enum State { SETUP, RUNNING, FINISHED }

  private State state;
  private Player winner;
  private List<Player> players;
  private Deck deck;
  private Board board;

  private final Random random = new Random();

  public Game() {
    this.state  = State.SETUP;
    this.winner = null;
    this.players = new ArrayList<>();
  }

  public State getState()   { return state; }
  public Player getWinner() { return winner; }
  public Board getBoard()   { return board; }
  public Deck getDeck()     { return deck; }
  public List<Player> getPlayers() { return players; }

  /**
   * Initialisiert alle Entities und startet das Spiel.
   *
   * @param playerNames     Namen der Spieler
   * @param allActionCards  Alle Aktionskarten (ohne Doom & Snack Stash)
   * @param snackStashProto Prototyp-Karte für den Snack Stash
   * @param doomProtos      Doom-Hamster-Karten
   */
  public void setup(
    List<String> playerNames,
    List<Card>   allActionCards,
    Card         snackStashProto,
    List<Card>   doomProtos
  ) {
    if (playerNames.size() < 2) {
      throw new IllegalArgumentException("Mindestens 2 Spieler erforderlich");
    }

    // Spieler erstellen
    players = new ArrayList<>();
    for (int i = 0; i < playerNames.size(); i++) {
      players.add(new Player("p" + i, playerNames.get(i)));
    }

    // Deck mischen (ohne Doom)
    deck = new Deck(allActionCards);
    deck.shuffle();

    // Jeder Spieler erhält 1 Snack Stash + 5 zufällige Karten
    for (Player player : players) {
      Card snackStash = new Card(
        "ss_" + player.getId(),
        snackStashProto.getName(),
        "snack_stash"
      );
      player.addToHand(snackStash);

      List<Card> drawn = deck.drawMultiple(STARTING_HAND_SIZE - 1);
      drawn.forEach(player::addToHand);
    }

    // Doom-Karten einmischen: Spieleranzahl − 1
    int doomCount = playerNames.size() - 1;
    List<Card> doomCards = doomProtos.subList(0, Math.min(doomCount, doomProtos.size()));
    deck.insertDoomCards(doomCards);

    board = new Board(players, deck);

    // Startspieler zufällig bestimmen
    board.setCurrentIndex(random.nextInt(players.size()));

    state = State.RUNNING;
  }

  /** Prüft die Siegbedingung und setzt ggf. den Gewinner */
  public boolean checkWinCondition() {
    List<Player> alive = board.getActivePlayers();
    if (alive.size() == 1) {
      winner = alive.get(0);
      state  = State.FINISHED;
      return true;
    }
    return false;
  }

  /**
   * Führt einen vollständigen Zug aus.
   *
   * @param cardIds IDs der zu spielenden Karten (kann leer sein)
   */
  public void executeTurn(List<String> cardIds) {
    if (state != State.RUNNING) return;

    Player player = board.getCurrentPlayer();

    // a) Karten spielen
    for (String id : cardIds) {
      Card played = player.playCard(id, this);
      board.discardCard(played);
    }

    // b) 1 Karte ziehen (Pflicht)
    Card drawn = deck.draw();
    if (drawn == null) return; // leeres Deck

    if (drawn.isDoom()) {
      Player.DoomResult result = player.handleDoom();
      if (!result.neutralized) {
        deck.discard(drawn);
      }
    } else {
      player.addToHand(drawn);
    }

    // c) Siegbedingung prüfen
    if (checkWinCondition()) return;

    // d) Nächster Spieler
    board.advanceTurn();
  }
}

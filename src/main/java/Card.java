import java.util.function.BiConsumer;

public class Card {
  private final String id;
  private final String name;
  private final String type; // "action" | "snack_stash" | "doom"
  private final BiConsumer<Game, Player> effect;

  public Card(String id, String name, String type, BiConsumer<Game, Player> effect) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.effect = effect;
  }

  public Card(String id, String name, String type) {
    this(id, name, type, null);
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }


  public boolean isDoom() {
    return "doom".equals(type);
  }

  public boolean isSnackStash() {
    return "snack_stash".equals(type);
  }

  /**
   * Führt den Karteneffekt aus, falls vorhanden
   */
  public void play(Game game, Player player) {
    if (effect != null) {
      effect.accept(game, player);
    }
  }
}

import java.util.function.BiConsumer;

/**
 * Represents a single card in the Doom Hamster game.
 *
 * <p>A card has a type ({@code action}, {@code snack_stash}, or {@code doom}) and an optional
 * effect that is executed when the card is played.
 */
public class Card {

  private final String id;
  private final String name;
  private final String type;
  private final BiConsumer<Game, Player> effect;

  /**
   * Creates a card with an effect.
   *
   * @param id     the unique identifier of the card
   * @param name   the display name of the card
   * @param type   the card type; one of {@code "action"}, {@code "snack_stash"}, or {@code "doom"}
   * @param effect the effect to execute when the card is played, or {@code null} for no effect
   */
  public Card(String id, String name, String type, BiConsumer<Game, Player> effect) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.effect = effect;
  }

  /**
   * Creates a card without an effect.
   *
   * @param id   the unique identifier of the card
   * @param name the display name of the card
   * @param type the card type; one of {@code "action"}, {@code "snack_stash"}, or {@code "doom"}
   */
  public Card(String id, String name, String type) {
    this(id, name, type, null);
  }

  /**
   * Returns the unique identifier of this card.
   *
   * @return the card id
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the display name of this card.
   *
   * @return the card name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the type of this card.
   *
   * @return one of {@code "action"}, {@code "snack_stash"}, or {@code "doom"}
   */
  public String getType() {
    return type;
  }

  /**
   * Returns whether this card is a Doom Hamster card.
   *
   * @return {@code true} if the card type is {@code "doom"}
   */
  public boolean isDoom() {
    return "doom".equals(type);
  }

  /**
   * Returns whether this card is a Snack Stash card.
   *
   * @return {@code true} if the card type is {@code "snack_stash"}
   */
  public boolean isSnackStash() {
    return "snack_stash".equals(type);
  }

  /**
   * Plays this card, executing its effect if one is present.
   *
   * @param game   the current game instance
   * @param player the player who played this card
   */
  public void play(Game game, Player player) {
    if (effect != null) {
      effect.accept(game, player);
    }
  }
}

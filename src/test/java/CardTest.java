import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CardTest {

  private static Card actionCard() {
    return new Card("a1", "Action Card", "action");
  }

  private static Card snackStashCard() {
    return new Card("s1", "Snack Stash", "snack_stash");
  }

  private static Card doomCard() {
    return new Card("d1", "Doom Hamster", "doom");
  }

  @Test
  void constructorShouldInitializeFieldsCorrectly() {
    Card card = new Card("1", "Test Card", "action");

    assertEquals("1", card.getId());
    assertEquals("Test Card", card.getName());
    assertEquals("action", card.getType());
  }

  @Test
  void constructorWithEffectShouldStoreEffect() {
    BiConsumer<Game, Player> effect = (g, p) -> {
    };
    Card card = new Card("1", "Effect Card", "action", effect);

    assertNotNull(card);
  }

  @Test
  void isDoomShouldReturnTrueOnlyForDoomType() {
    Card doomCard = new Card("1", "Doom Card", "doom");
    Card normalCard = new Card("2", "Normal Card", "action");

    assertTrue(doomCard.isDoom());
    assertFalse(normalCard.isDoom());
  }

  @Test
  void isSnackStashShouldReturnTrueOnlyForSnackStashType() {
    Card snackCard = new Card("1", "Snack Card", "snack_stash");
    Card normalCard = new Card("2", "Normal Card", "action");

    assertTrue(snackCard.isSnackStash());
    assertFalse(normalCard.isSnackStash());
  }

  @Test
  void playShouldExecuteEffectWhenPresent() {
    AtomicBoolean executed = new AtomicBoolean(false);

    BiConsumer<Game, Player> effect = (g, p) -> executed.set(true);
    Card card = new Card("1", "Effect Card", "action", effect);

    card.play(null, null);

    assertTrue(executed.get());
  }

  @Test
  void playShouldNotThrowWhenEffectIsNull() {
    Card card = new Card("1", "No Effect Card", "action");

    assertDoesNotThrow(() -> card.play(null, null));
  }

  @Test
  void playShouldPassCorrectParametersToEffect() {
    Game game = new Game();
    Player player = new Player("69", "joe");

    AtomicBoolean correctParams = new AtomicBoolean(false);

    BiConsumer<Game, Player> effect = (g, p) -> {
      if (g == game && p == player) {
        correctParams.set(true);
      }
    };

    Card card = new Card("1", "Effect Card", "action", effect);

    card.play(game, player);

    assertTrue(correctParams.get());
  }
  @Test
  @DisplayName("returns true for type 'doom'")
  void trueForDoom() {
    assertTrue(doomCard().isDoom());
  }

  @Test
  @DisplayName("returns false for type 'action'")
  void falseForAction() {
    assertFalse(actionCard().isDoom());
  }

  @Test
  @DisplayName("returns false for type 'snack_stash'")
  void falseForSnackStash() {
    assertFalse(snackStashCard().isDoom());
  }

  @Test
  @DisplayName("returns false for type 'DOOM' (case-sensitive)")
  void caseSensitive() {
    Card card = new Card("d2", "Doom", "DOOM");
    assertFalse(card.isDoom());
  }

  @Test
  @DisplayName("returns false for empty type string")
  void falseForEmptyType() {
    Card card = new Card("x1", "Name", "");
    assertFalse(card.isDoom());
  }

  @Test
  @DisplayName("executes effect when one is present")
  void executesEffect() {
    boolean[] called = {false};
    Card card = new Card("a1", "Test", "action", (game, player) -> called[0] = true);
    card.play(null, null);
    assertTrue(called[0]);
  }

  @Test
  @DisplayName("does not throw when effect is null")
  void noThrowWhenEffectNull() {
    assertDoesNotThrow(() -> actionCard().play(null, null));
  }

  @Test
  @DisplayName("passes the game argument to the effect")
  void passesGameToEffect() {
    Game mockGame = new Game();
    Game[] received = {null};
    Card card = new Card("a1", "Test", "action", (game, player) -> received[0] = game);
    card.play(mockGame, null);
    assertSame(mockGame, received[0]);
  }

  @Test
  @DisplayName("passes the player argument to the effect")
  void passesPlayerToEffect() {
    Player mockPlayer = new Player("p0", "Alice");
    Player[] received = {null};
    Card card = new Card("a1", "Test", "action", (game, player) -> received[0] = player);
    card.play(null, mockPlayer);
    assertSame(mockPlayer, received[0]);
  }

  @Test
  @DisplayName("effect is called exactly once per play() invocation")
  void effectCalledExactlyOnce() {
    int[] callCount = {0};
    Card card = new Card("a1", "Test", "action", (game, player) -> callCount[0]++);
    card.play(null, null);
    assertEquals(1, callCount[0]);
  }

}


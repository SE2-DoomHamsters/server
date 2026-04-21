import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

public class CardTest {


  @Test
  void constructorShouldInitializeFieldsCorrectly() {
    Card card = new Card("1", "Test Card", "action");

    assertEquals("1", card.getId());
    assertEquals("Test Card", card.getName());
    assertEquals("action", card.getType());
  }

  @Test
  void constructorWithEffectShouldStoreEffect() {
    BiConsumer<Game, Player> effect = (g, p) -> {};
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
    Player player = new Player("69","joe");

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
}


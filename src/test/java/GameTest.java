import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class GameTest {

  static List<Card> makeActionCards(int count) {
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      cards.add(new Card("action_" + i, "Action Card " + i, "action"));
    }
    return cards;
  }

  private static Card makeSnackStash() {
    return new Card("ss_proto", "Snack Stash", "snack_stash");
  }

  static List<Card> makeDoomCards(int count) {
    List<Card> cards = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      cards.add(new Card("doom_" + i, "Doom Hamster " + i, "doom"));
    }
    return cards;
  }

  /**
   * Sets up a ready-to-run game with enough cards for n players.
   */
  private static Game setupGame(List<String> playerNames) {
    Game game = new Game();
    // Each player draws 5 action cards, so we need at least playerCount * 5
    int cardCount = playerNames.size() * 5 + 20;
    game.setup(
        playerNames,
        makeActionCards(cardCount),
        makeSnackStash(),
        makeDoomCards(playerNames.size() + 5) // extra doom cards as buffer
    );
    return game;
  }


  @Nested
  @DisplayName("Card")
  class CardTests {

    @Test
    @DisplayName("isDoom() returns true only for doom type")
    void isDoom() {
      assertTrue(new Card("d1", "Doom", "doom").isDoom());
      assertFalse(new Card("a1", "Action", "action").isDoom());
      assertFalse(new Card("s1", "Snack", "snack_stash").isDoom());
    }

    @Test
    @DisplayName("isSnackStash() returns true only for snack_stash type")
    void isSnackStash() {
      assertTrue(new Card("s1", "Snack Stash", "snack_stash").isSnackStash());
      assertFalse(new Card("d1", "Doom", "doom").isSnackStash());
      assertFalse(new Card("a1", "Action", "action").isSnackStash());
    }

    @Test
    @DisplayName("play() triggers effect with correct arguments")
    void playCallsEffect() {
      boolean[] called = {false};
      Card card = new Card("a1", "Test", "action",
          (game, player) -> called[0] = true);

      card.play(null, null);
      assertTrue(called[0]);
    }

    @Test
    @DisplayName("play() on a card without effect does not throw")
    void playWithNoEffect() {
      Card card = new Card("d1", "Doom", "doom");
      assertDoesNotThrow(() -> card.play(null, null));
    }
  }


}

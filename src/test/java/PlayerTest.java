import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PlayerTest {


  private Player player;

  @BeforeEach
  void init() {
    player = new Player("p0", "Alice");
  }

  @Test
  @DisplayName("new player starts with 3 lives")
  void startingLives() {
    assertEquals(3, player.getLives());
  }

  @Test
  @DisplayName("new player is alive and not eliminated")
  void initiallyAlive() {
    assertTrue(player.isAlive());
    assertFalse(player.isEliminated());
  }

  @Test
  @DisplayName("addToHand() increases hand size")
  void addToHand() {
    player.addToHand(new Card("a1", "Test", "action"));
    assertEquals(1, player.getHand().size());
  }

  @Test
  @DisplayName("removeFromHand() returns and removes correct card")
  void removeFromHand() {
    Card card = new Card("a1", "Test", "action");
    player.addToHand(card);
    Card removed = player.removeFromHand("a1");
    assertEquals(card, removed);
    assertTrue(player.getHand().isEmpty());
  }

  @Test
  @DisplayName("removeFromHand() returns null for unknown id")
  void removeFromHandMissing() {
    assertNull(player.removeFromHand("nonexistent"));
  }

  @Test
  @DisplayName("hasSnackStash() is true when stash is in hand")
  void hasSnackStash() {
    assertFalse(player.hasSnackStash());
    player.addToHand(new Card("ss1", "Snack Stash", "snack_stash"));
    assertTrue(player.hasSnackStash());
  }

  // handleDoom – no snack stash

  @Test
  @DisplayName("handleDoom() without snack stash reduces lives by 1")
  void handleDoomLosesLife() {
    Player.DoomResult result = player.handleDoom();
    assertFalse(result.neutralized);
    assertEquals(2, player.getLives());
  }

  @Test
  @DisplayName("handleDoom() at 1 life eliminates the player")
  void handleDoomElimination() {
    player.handleDoom(); // 3 → 2
    player.handleDoom(); // 2 → 1
    player.handleDoom(); // 1 → 0 → eliminated
    assertFalse(player.isAlive());
    assertTrue(player.isEliminated());
  }

  // handleDoom – with snack stash

  @Test
  @DisplayName("handleDoom() with snack stash neutralizes doom")
  void handleDoomNeutralized() {
    player.addToHand(new Card("ss1", "Snack Stash", "snack_stash"));
    Player.DoomResult result = player.handleDoom();
    assertTrue(result.neutralized);
    assertEquals(3, player.getLives()); // lives unchanged
  }

  @Test
  @DisplayName("handleDoom() consumes the snack stash")
  void handleDoomConsumesStash() {
    player.addToHand(new Card("ss1", "Snack Stash", "snack_stash"));
    player.handleDoom();
    assertFalse(player.hasSnackStash());
  }

  @Test
  @DisplayName("playCard() removes card from hand and triggers effect")
  void playCard() {
    boolean[] fired = {false};
    Card card = new Card("a1", "Test", "action",
        (g, p) -> fired[0] = true);
    player.addToHand(card);
    player.playCard("a1", null);
    assertTrue(fired[0]);
    assertTrue(player.getHand().isEmpty());
  }

  @Test
  @DisplayName("playCard() throws when card not in hand")
  void playCardNotInHand() {
    assertThrows(IllegalArgumentException.class,
        () -> player.playCard("missing", null));
  }
}



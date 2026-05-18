
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.doomhamsters.Player;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link GameState}. */
final class GameStateTest {

  static List<Player> twoPlayers() {
    return List.of(new Player("p1", "sus"), new Player("p2", "sus"));
  }

  @Test
  void getGameId_returnsConstructorValue() {
    GameState state = new GameState("g1", twoPlayers(), "p1", false, null);
    assertThat(state.getGameId()).isEqualTo("g1");
  }

  @Test
  void getCurrentTurnPlayerId_returnsConstructorValue() {
    GameState state = new GameState("g1", twoPlayers(), "p2", false, null);
    assertThat(state.getCurrentTurnPlayerId()).isEqualTo("p2");
  }

  @Test
  void isSnackStashPending_returnsFalseWhenNotPending() {
    GameState state = new GameState("g1", twoPlayers(), "p1", false, null);
    assertThat(state.isSnackStashPending()).isFalse();
  }

  @Test
  void isSnackStashPending_returnsTrueWhenPending() {
    GameState state = new GameState("g1", twoPlayers(), "p1", true, "p1");
    assertThat(state.isSnackStashPending()).isTrue();
  }

  @Test
  void getPendingPlayerId_returnsNullWhenNotPending() {
    GameState state = new GameState("g1", twoPlayers(), "p1", false, null);
    assertThat(state.getPendingPlayerId()).isNull();
  }

  @Test
  void getPendingPlayerId_returnsCorrectIdWhenPending() {
    GameState state = new GameState("g1", twoPlayers(), "p1", true, "p1");
    assertThat(state.getPendingPlayerId()).isEqualTo("p1");
  }

  @Test
  void getPlayers_returnsDefensiveCopy() {
    List<Player> original = new ArrayList<>(twoPlayers());
    GameState state = new GameState("g1", original, "p1", false, null);
    // Mutating the returned list must not affect the state's internal list.
    assertThrows(UnsupportedOperationException.class, () -> state.getPlayers().clear());
  }
}




import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

  /** Unit tests for {@link Player}. */
  final class PlayerTest {

    @Test
    void constructor_setsIdAndLives() {
      Player player = new Player("alice", 3);
      assertThat(player.getId()).isEqualTo("alice");
      assertThat(player.getLives()).isEqualTo(3);
    }

    @Test
    void constructor_isNotEliminatedByDefault() {
      Player player = new Player("bob", 2);
      assertThat(player.isEliminated()).isFalse();
    }

    @Test
    void decrementLives_reducesLivesByOne() {
      Player player = new Player("carol", 3);
      player.decrementLives();
      assertThat(player.getLives()).isEqualTo(2);
    }

    @Test
    void decrementLives_doesNotEliminatePlayerWhileLivesRemain() {
      Player player = new Player("dave", 2);
      player.decrementLives();
      assertThat(player.isEliminated()).isFalse();
    }

    @Test
    void decrementLives_eliminatesPlayerWhenLivesReachZero() {
      Player player = new Player("eve", 1);
      player.decrementLives();
      assertThat(player.getLives()).isEqualTo(0);
      assertThat(player.isEliminated()).isTrue();
    }

    @Test
    void decrementLives_isNoOpOnEliminatedPlayer() {
      Player player = new Player("frank", 1);
      player.decrementLives(); // eliminated here
      player.decrementLives(); // should be ignored
      assertThat(player.getLives()).isEqualTo(0);
      assertThat(player.isEliminated()).isTrue();
    }

    @Test
    void decrementLives_livesNeverGoBelowZero() {
      Player player = new Player("grace", 1);
      player.decrementLives();
      player.decrementLives();
      player.decrementLives();
      assertThat(player.getLives()).isEqualTo(0);
    }

    @Test
    void multipleDecrements_trackLivesCorrectly() {
      Player player = new Player("hal", 5);
      player.decrementLives();
      player.decrementLives();
      player.decrementLives();
      assertThat(player.getLives()).isEqualTo(2);
      assertThat(player.isEliminated()).isFalse();
    }
  }


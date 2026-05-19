
package com.doomhamsters;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link DrawRequest}. */
final class DrawRequestTest {

  @Test
  void noArgConstructor_leavesPlayerIdNull() {
    DrawRequest request = new DrawRequest();
    assertThat(request.getPlayerId()).isNull();
  }

  @Test
  void allArgConstructor_setsPlayerId() {
    DrawRequest request = new DrawRequest("player-7");
    assertThat(request.getPlayerId()).isEqualTo("player-7");
  }

  @Test
  void setPlayerId_updatesValue() {
    DrawRequest request = new DrawRequest();
    request.setPlayerId("player-3");
    assertThat(request.getPlayerId()).isEqualTo("player-3");
  }

  @Test
  void setPlayerId_overwritesExistingValue() {
    DrawRequest request = new DrawRequest("old");
    request.setPlayerId("new");
    assertThat(request.getPlayerId()).isEqualTo("new");
  }

  @Test
  void setPlayerId_acceptsNull() {
    DrawRequest request = new DrawRequest("player-1");
    request.setPlayerId(null);
    assertThat(request.getPlayerId()).isNull();
  }
}

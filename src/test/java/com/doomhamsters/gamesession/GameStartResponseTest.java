package com.doomhamsters.gamesession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class GameStartResponseTest {

  @Test
  void testGetterAndConstructor() {
    String expectedId = "test-game-id";
    GameStartResponse response = new GameStartResponse(expectedId);

    assertEquals(expectedId, response.getGameId(), "The gameId should match the constructor input");
  }
}

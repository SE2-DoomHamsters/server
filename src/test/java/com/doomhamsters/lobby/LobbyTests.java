package com.doomhamsters.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Test class for the Lobby model class.
 * Covers all constructors, getters, and setters.
 */
class LobbyTest {

  @Test
  void testNoArgsConstructorAndSetters() {
    Lobby lobby = new Lobby();
    List<User> members = new ArrayList<>();
    members.add(new User("1", "Tester", "🐹"));

    lobby.setLobbyId("TEST_LOBBY");
    lobby.setMembers(members);
    lobby.setQrCodeBase64("base64string");
    lobby.setGameId("game-123");
    lobby.setGameStarted(true);

    assertEquals("TEST_LOBBY", lobby.getLobbyId());
    assertEquals(1, lobby.getMembers().size());
    assertEquals("base64string", lobby.getQrCodeBase64());
    assertEquals("game-123", lobby.getGameId());
    assertTrue(lobby.isGameStarted());
  }

  @Test
  void testConstructorWithId() {
    Lobby lobby = new Lobby("QUICK_START");

    assertEquals("QUICK_START", lobby.getLobbyId());
    assertNotNull(lobby.getMembers());
    assertTrue(lobby.getMembers().isEmpty());
  }

  @Test
  void testMembersListIsModifiable() {
    Lobby lobby = new Lobby("LIST_TEST");
    User user = new User("99", "Player", "🐱");

    // Modification: retrieve copy, add user, reset list
    List<User> members = lobby.getMembers();
    members.add(user);
    lobby.setMembers(members);

    assertEquals(1, lobby.getMembers().size());
    assertEquals(user, lobby.getMembers().get(0));
  }
  @Test
  void testMembersNullHandling() {
    Lobby lobby = new Lobby();

    // Tests the null branch in setMembers
    lobby.setMembers(null);
    assertNotNull(lobby.getMembers());
    assertTrue(lobby.getMembers().isEmpty());
  }


  @Test
  void testGetMembersWhenInternalListIsNullReturnsEmptySnapshot() throws Exception {
    Lobby lobby = new Lobby();
    // Uses reflection to explicitly set the private field to null
    java.lang.reflect.Field field = Lobby.class.getDeclaredField("members");
    field.setAccessible(true);
    field.set(lobby, null);

    assertNotNull(lobby.getMembers());
    assertTrue(lobby.getMembers().isEmpty());
  }
}

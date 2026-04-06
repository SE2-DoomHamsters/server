package com.doomhamsters.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Testklasse für die Lobby-Modellklasse.
 * Deckt alle Konstruktoren, Getter und Setter ab.
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

    assertEquals("TEST_LOBBY", lobby.getLobbyId());
    assertEquals(1, lobby.getMembers().size());
    assertEquals("base64string", lobby.getQrCodeBase64());
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

    lobby.getMembers().add(user);

    assertEquals(1, lobby.getMembers().size());
    assertEquals(user, lobby.getMembers().get(0));
  }
}

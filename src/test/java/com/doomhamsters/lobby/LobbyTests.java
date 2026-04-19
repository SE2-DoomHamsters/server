package com.doomhamsters.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    // Änderung: Kopie holen, User hinzufügen, Liste zurücksetzen
    List<User> members = lobby.getMembers();
    members.add(user);
    lobby.setMembers(members);

    assertEquals(1, lobby.getMembers().size());
    assertEquals(user, lobby.getMembers().get(0));
  }
  @Test
  void testMembersNullHandling() {
    Lobby lobby = new Lobby();

    // Testet den Null-Branch in setMembers
    lobby.setMembers(null);
    assertNotNull(lobby.getMembers());
    assertTrue(lobby.getMembers().isEmpty());
  }


  @Test
  void testGetMembersWhenNull() throws Exception {
    Lobby lobby = new Lobby();
    // Ich nutze Reflection, um das private Feld hart auf null zu setzen,
    java.lang.reflect.Field field = Lobby.class.getDeclaredField("members");
    field.setAccessible(true);
    field.set(lobby, null);

    assertNull(lobby.getMembers()); // Deckt den (members == null) Branch in getMembers ab
  }
}

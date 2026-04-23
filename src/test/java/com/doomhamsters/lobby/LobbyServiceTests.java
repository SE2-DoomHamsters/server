package com.doomhamsters.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

/**
 * Testklasse für den LobbyService.
 * Prüft die Erstellung, das Beitreten und die QR-Generierung.
 */
class LobbyServiceTest {

  private LobbyService lobbyService;
  private User testCreator;

  @BeforeEach
  void setUp() {
    lobbyService = new LobbyService();
    testCreator = new User("session-1", "AdminHamster", "👑");
  }

  @Test
  void testCreateLobby() {
    Lobby lobby = lobbyService.createLobby("Meine Gruppe", testCreator);

    assertNotNull(lobby);
    assertEquals("MEINE_GRUPPE", lobby.getLobbyId());
    assertEquals(1, lobby.getMembers().size());
    assertEquals(testCreator, lobby.getMembers().get(0));
    assertNotNull(lobby.getQrCodeBase64());
  }

  @Test
  void testJoinExistingLobby() {
    lobbyService.createLobby("Test", testCreator);
    User newUser = new User("session-2", "Gast", "🐹");

    Optional<Lobby> result = lobbyService.joinOrUpdateLobby("TEST", newUser);

    assertTrue(result.isPresent()); // Prüfen, ob das Optional gefüllt ist
    Lobby joinedLobby = result.get(); // Den Inhalt aus dem Optional herausholen
    assertEquals(2, joinedLobby.getMembers().size());
  }

  @Test
  void testUpdateUserInLobby() {
    lobbyService.createLobby("Test", testCreator);
    // Gleiche ID, aber neuer Name/Avatar
    User updatedUser = new User("session-1", "NeuerName", "🦊");

    Optional<Lobby> lobbyOptional = lobbyService.joinOrUpdateLobby("TEST", updatedUser);

    assertTrue(lobbyOptional.isPresent(), "Lobby sollte vorhanden sein");

    Lobby lobby = lobbyOptional.get();

    assertEquals(1, lobby.getMembers().size());
    assertEquals("NeuerName", lobby.getMembers().get(0).getUsername());
  }

  @Test
  void testJoinNonExistentLobby() {
    User newUser = new User("session-2", "Gast", "🐹");
    Optional<Lobby> result = lobbyService.joinOrUpdateLobby("GIBTS_NICHT", newUser);

    assertTrue(result.isEmpty()); // Prüfen, ob es wirklich leer ist (statt assertNull)
  }

  @Test
  void testGetLobby() {
    lobbyService.createLobby("FindMich", testCreator);

    assertNotNull(lobbyService.getLobby("FINDMICH"));
    assertNull(lobbyService.getLobby("WEG"));
  }
  @Test
  void testGenerateQrCodeError() {
    // Ein extrem langer String (über 3000 Zeichen) sprengt die Kapazität eines Standard-QR-Codes
    String tooLong = "A".repeat(5000);

    // Ruft indirekt generateQrCode über createLobby auf
    Lobby lobby = lobbyService.createLobby(tooLong, testCreator);

    // Wenn die Exception geworfen wurde, ist der Base64 String null
    assertNull(lobby.getQrCodeBase64());
  }
  @Test
  void testGenerateQrCodeBranches() {

    Lobby lobbyNull = lobbyService.createLobby("", testCreator);

    Lobby lobbyBlank = lobbyService.createLobby("   ", testCreator);
    assertNull(lobbyBlank.getQrCodeBase64());
  }

  @Test
  void testGetLobbyBranch() {
    assertNull(lobbyService.getLobby("EXISTIERT_NICHT"));
  }
  @Test
  void testGenerateQrCodeDirectly() throws Exception {
    java.lang.reflect.Method method = LobbyService.class.getDeclaredMethod("generateQrCode", String.class);
    method.setAccessible(true);

    assertNull(method.invoke(lobbyService, (Object) null));

    assertNull(method.invoke(lobbyService, ""));
    assertNull(method.invoke(lobbyService, "   "));
  }
}

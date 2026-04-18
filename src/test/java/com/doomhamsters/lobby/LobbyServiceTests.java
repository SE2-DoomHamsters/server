package com.doomhamsters.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    Lobby joinedLobby = lobbyService.joinOrUpdateLobby("TEST", newUser);

    assertNotNull(joinedLobby);
    assertEquals(2, joinedLobby.getMembers().size());
    assertTrue(joinedLobby.getMembers().contains(newUser));
  }

  @Test
  void testUpdateUserInLobby() {
    lobbyService.createLobby("Test", testCreator);
    // Gleiche ID, aber neuer Name/Avatar
    User updatedUser = new User("session-1", "NeuerName", "🦊");

    Lobby lobby = lobbyService.joinOrUpdateLobby("TEST", updatedUser);

    assertEquals(1, lobby.getMembers().size());
    assertEquals("NeuerName", lobby.getMembers().get(0).getUsername());
  }

  @Test
  void testJoinNonExistentLobby() {
    User newUser = new User("session-2", "Gast", "🐹");
    Lobby result = lobbyService.joinOrUpdateLobby("GIBTS_NICHT", newUser);

    assertNull(result);
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
}

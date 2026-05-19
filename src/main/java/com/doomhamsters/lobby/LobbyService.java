package com.doomhamsters.lobby;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Service zur Verwaltung der Lobbys und QR-Code Generierung.
 */
@Service
public class LobbyService {

  private final Map<String, Lobby> activeLobbies = new ConcurrentHashMap<>();

  /**
   * Erstellt eine neue Lobby basierend auf einem Gruppennamen.
   *
   * @param groupName Der eingegebene Name der Gruppe
   * @param creator Der Benutzer, der diese Lobby erstellt
   * @return Die neu generierte Lobby
   */
  public Lobby createLobby(String groupName, User creator) {
    String lobbyId = groupName.toUpperCase().trim().replaceAll("\\s+", "_");
    Lobby lobby = new Lobby(lobbyId);

    // FIX: Liste holen, ändern, setzen
    List<User> members = new ArrayList<>();
    members.add(creator);
    lobby.setMembers(members);

    lobby.setQrCodeBase64(generateQrCode(lobbyId));
    activeLobbies.put(lobbyId, lobby);
    return lobby;
  }

  /**
   * Fügt einen User einer bestehenden Lobby hinzu oder aktualisiert seine Daten.
   *
   * @param lobbyId Die ID der Lobby, der beigetreten werden soll
   * @param user Der Benutzer, der beitritt oder aktualisiert wird
   * @return Ein Optional mit der aktualisierten Lobby, falls sie existiert
   */
  public Optional<Lobby> joinOrUpdateLobby(String lobbyId, User user) {
    Lobby lobby = activeLobbies.get(lobbyId);
    if (lobby != null) {
      // 1. Die Kopie der Liste holen
      List<User> currentMembers = lobby.getMembers();

      // 2. In der Kopie arbeiten
      currentMembers.removeIf(m -> m.getId().equals(user.getId()));
      currentMembers.add(user);

      // 3. Die geänderte Liste mit dem Setter zurückspeichern
      lobby.setMembers(currentMembers);

      return Optional.of(lobby);
    }
    return Optional.empty();
  }

  private String generateQrCode(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    try {
      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
      return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    } catch (com.google.zxing.WriterException | java.io.IOException e) {

      return null;
    }
  }

  /**
   * Sucht und liefert eine aktive Lobby anhand ihrer ID.
   *
   * @param lobbyId Die ID der gesuchten Lobby
   * @return Die gefundene Lobby oder null, falls sie nicht existiert
   */
  public Lobby getLobby(String lobbyId) {
    return activeLobbies.get(lobbyId);
  }
}

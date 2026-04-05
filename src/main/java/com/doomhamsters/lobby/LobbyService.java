package com.doomhamsters.lobby;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Service zur Verwaltung der Lobbys und QR-Code Generierung.
 */
@Service
public class LobbyService {

  private final Map<String, Lobby> activeLobbies = new ConcurrentHashMap<>();

  /** Erstellt eine neue Lobby. */
  public Lobby createLobby(String groupName, User creator) {
    String lobbyId = groupName.toUpperCase().trim().replaceAll("\\s+", "_");
    Lobby lobby = new Lobby(lobbyId);
    lobby.getMembers().add(creator);
    lobby.setQrCodeBase64(generateQrCode(lobbyId));
    activeLobbies.put(lobbyId, lobby);
    return lobby;
  }

  /** Fügt einen User hinzu oder aktualisiert ihn. */
  public Lobby joinOrUpdateLobby(String lobbyId, User user) {
    Lobby lobby = activeLobbies.get(lobbyId);
    if (lobby != null) {
      lobby.getMembers().removeIf(m -> m.getId().equals(user.getId()));
      lobby.getMembers().add(user);
      return lobby;
    }
    return null;
  }

  private String generateQrCode(String text) {
    try {
      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
      return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    } catch (Exception e) {
      return null;
    }
  }

  /** Liefert eine Lobby per ID. */
  public Lobby getLobby(String lobbyId) {
    return activeLobbies.get(lobbyId);
  }
}

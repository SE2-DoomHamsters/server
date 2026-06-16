package com.doomhamsters.lobby;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class QrCodeGeneratorServiceTest {

  private QrCodeGeneratorService service;

  @BeforeEach
  void setUp() {
    service = new QrCodeGeneratorService();
  }

  @Test
  void validTextGeneratesBase64String() {
    String result = service.generateQrCode("LOBBY-123");

    assertNotNull(result);
    // Ein gültiger Base64 PNG-String sollte immer generiert werden
    assertTrue(result.length() > 50);
  }

  @Test
  void nullOrBlankTextReturnsNull() {
    assertNull(service.generateQrCode(null));
    assertNull(service.generateQrCode(""));
    assertNull(service.generateQrCode("   "));
  }

  @Test
  void catchBlockIsCoveredOnWriterException() {
    // Wir fangen die interne Erstellung von QRCodeWriter ab und zwingen sie, einen Fehler zu werfen
    try (MockedConstruction<QRCodeWriter> mocked = Mockito.mockConstruction(QRCodeWriter.class,
      (mock, context) -> {
        Mockito.when(mock.encode(Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
          .thenThrow(new WriterException("Simulierter Fehler für 100% Coverage"));
      })) {

      String result = service.generateQrCode("LOBBY-123");
      assertNull(result); // Der catch-Block loggt den Fehler und gibt null zurück
    }
  }
}

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
    // A valid Base64 PNG string should always be generated
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
    // Intercept the internal construction of QRCodeWriter and force it to throw an exception
    try (MockedConstruction<QRCodeWriter> mocked = Mockito.mockConstruction(QRCodeWriter.class,
      (mock, context) -> {
        Mockito.when(mock.encode(Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
          .thenThrow(new WriterException("Simulated error for 100% coverage"));
      })) {

      String result = service.generateQrCode("LOBBY-123");
      assertNull(result); // The catch block logs the error and returns null
    }
  }
}

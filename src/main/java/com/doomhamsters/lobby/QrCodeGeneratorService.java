package com.doomhamsters.lobby;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service für die Generierung von QR-Code Bildern.
 */
@Service
public class QrCodeGeneratorService {

  private static final Logger LOGGER = LoggerFactory.getLogger(QrCodeGeneratorService.class);

  /**
   * Generiert einen QR-Code aus einem gegebenen Text und gibt diesen als Base64-String zurück.
   *
   * @param text Der Text, der im QR-Code verschlüsselt werden soll.
   * @return Der Base64 codierte PNG-String oder null im Fehlerfall.
   */
  public String generateQrCode(String text) {
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
      LOGGER.error("Failed to generate QR code for text: {}", text, e);
      return null;
    }
  }
}

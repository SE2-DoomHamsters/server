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
 * Service for generating QR code images.
 */
@Service
public class QrCodeGeneratorService {

  private static final Logger LOGGER = LoggerFactory.getLogger(QrCodeGeneratorService.class);

  /**
   * Generates a QR code from a given text and returns it as a Base64 string.
   *
   * @param text the text to be encoded into the QR code
   * @return the Base64-encoded PNG string, or null in case of an error
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

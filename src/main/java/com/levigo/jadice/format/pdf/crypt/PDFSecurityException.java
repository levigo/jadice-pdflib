package com.levigo.jadice.format.pdf.crypt;

/**
 * represents some kind of unexpected condition during the decryption process of a PDF stream.
 */
public class PDFSecurityException extends Exception {

  private static final long serialVersionUID = 1L;

  public PDFSecurityException(String message, Throwable cause) {
    super(message, cause);
  }

  public PDFSecurityException(String message) {
    super(message);
  }
}

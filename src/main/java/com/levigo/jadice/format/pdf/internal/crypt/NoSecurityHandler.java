package com.levigo.jadice.format.pdf.internal.crypt;

import com.levigo.jadice.format.pdf.crypt.PDFSecurityException;
import com.levigo.jadice.format.pdf.internal.objects.DSNameObject;


/**
 * a no-op security handler
 */
public class NoSecurityHandler implements SecurityHandler {

  private static final NoopDecryptor NO_DECRYPTION_HANDLER = new NoopDecryptor();

  @Override
  public boolean isAuthenticated() {
    return true;
  }

  @Override
  public PDFPermissions<?> getPermissions() {
    return null;
  }

  @Override
  public StringDecryptor getStringDecryptor() throws PDFSecurityException {
    return NO_DECRYPTION_HANDLER;
  }

  @Override
  public StreamDecryptor getStreamDecryptor() throws PDFSecurityException {
    return NO_DECRYPTION_HANDLER;
  }

  @Override
  public StreamDecryptor getStreamDecryptor(DSNameObject name) throws PDFSecurityException {
    return NO_DECRYPTION_HANDLER;
  }

  @Override
  public boolean authenticate() throws PDFSecurityException {
    return true;
  }

}

package com.levigo.jadice.format.pdf.internal.crypt;

import com.levigo.jadice.format.pdf.crypt.PDFSecurityException;
import com.levigo.jadice.format.pdf.internal.objects.DSNameObject;

/**
 * A security handler as defined by the PDF specification, PDF32000-1:2008, section 7.6. It performs
 * authorization and decryption of contents. It also provides a set of permissions in effect for
 * this PDF.
 */
public interface SecurityHandler {

  boolean authenticate() throws PDFSecurityException;

  boolean isAuthenticated() throws PDFSecurityException;

  StringDecryptor getStringDecryptor() throws PDFSecurityException;

  /**
   * @return the default {@link StreamDecryptor}. For security handlers smaller or equal to version
   * 3 there is only one stream decryptor. For version 4 and above there is the crypt filter
   * concept and the default {@link StreamDecryptor} is specified by StmF.
   * @throws PDFSecurityException
   */
  StreamDecryptor getStreamDecryptor() throws PDFSecurityException;

  StreamDecryptor getStreamDecryptor(DSNameObject name) throws PDFSecurityException;

  // DecryptionHandler getDecryptionHandler() throws PDFSecurityException;

  PDFPermissions<?> getPermissions() throws PDFSecurityException;

}

package com.levigo.jadice.format.pdf.internal.crypt;

import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.pdf.crypt.PDFSecurityException;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSString;

/**
 * performs a no-op decryption which means that input is passed through without any changes.
 */
public class NoopDecryptor implements StreamDecryptor, StringDecryptor {

  @Override
  public DSString getDecryptedString(DSString srcString, long objectNumber, long generationNumber)
      throws PDFSecurityException {
    return srcString;
  }

  @Override
  public SeekableInputStream getDecryptedStream(DSDictionary streamDictionary, SeekableInputStream sis) {
    return sis;
  }

}

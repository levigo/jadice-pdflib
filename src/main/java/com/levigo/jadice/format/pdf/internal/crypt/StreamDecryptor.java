package com.levigo.jadice.format.pdf.internal.crypt;

import java.io.InputStream;

import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.pdf.crypt.PDFSecurityException;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;

/**
 * implementations are able to decrypt contents of an {@link InputStream}
 */
public interface StreamDecryptor {

  SeekableInputStream getDecryptedStream(DSDictionary streamDictionary, SeekableInputStream sis)
      throws PDFSecurityException;

}

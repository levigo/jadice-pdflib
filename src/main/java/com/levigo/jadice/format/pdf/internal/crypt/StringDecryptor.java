package com.levigo.jadice.format.pdf.internal.crypt;

import com.levigo.jadice.format.pdf.crypt.PDFSecurityException;
import com.levigo.jadice.format.pdf.internal.objects.DSString;

/**
 * implementations are able to decrypt contents of {@link DSString} objects
 */
public interface StringDecryptor {

  DSString getDecryptedString(DSString srcString, long objectNumber, long generationNumber) throws PDFSecurityException;

}

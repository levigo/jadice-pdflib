package com.levigo.jadice.format.pdf.internal;

import java.io.IOException;

import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.pdf.crypt.PDFSecurityException;

public interface PDFDocumentFactory {

  PDFDocument create(final SeekableInputStream source) throws IOException, PDFSecurityException;

}

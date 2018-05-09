package com.levigo.jadice.format.pdf.internal.parsing;

import java.io.IOException;

import com.levigo.jadice.format.pdf.crypt.PDFSecurityException;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;

public interface IPDFParser {

  DSObject parseObject(long objectNumber, int generationNumber) throws IOException;

  DSObject parseObjectAt(long offset) throws IOException, RuntimeException, PDFSecurityException;

}

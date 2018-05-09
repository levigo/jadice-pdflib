package com.levigo.jadice.format.pdf.internal.objects;

import java.util.Arrays;

public class DSLiteralString extends DSString {
  public DSLiteralString(byte[] rawData) {
    super(rawData);
  }

  /**
   * checks for value equality
   *
   * @see com.levigo.jadice.format.pdf.internal.objects.DSObject#equals(com.levigo.jadice.format.pdf.internal.objects.DSObject)
   */
  @Override
  public boolean equals(DSObject object) {
    if (object instanceof DSLiteralString) {
      return Arrays.equals(getRawData(), ((DSLiteralString) object).getRawData());
    } else {
      return false;
    }
  }

  /**
   * This method is mainly used for the PDF-DocInspector
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return asAsciiString() + " (PDFLiteralString)";
  }
}

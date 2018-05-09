package com.levigo.jadice.format.pdf.internal.objects;

import java.util.Arrays;

/**
 * Holds a byte array with already parsed hex data as byte values
 */
public class DSHexString extends DSString {
  public DSHexString(byte[] rawData) {
    super(rawData);
  }

  /**
   * checks for value equality
   *
   * @see com.levigo.jadice.format.pdf.internal.objects.DSObject#equals(com.levigo.jadice.format.pdf.internal.objects.DSObject)
   */
  @Override
  public boolean equals(DSObject object) {
    if (object instanceof DSHexString) {
      return Arrays.equals(getRawData(), ((DSHexString) object).getRawData());
    } else {
      return false;
    }
  }

  /**
   * This method is mainly used for the PDF-DocInspector
   */
  @Override
  public String toString() {
    String retval = "";
    for (final byte element : getRawData()) {
      retval += showHex(element);
    }
    retval.trim();
    return "<" + retval + "> (PDFHexString)";
  }

  /**
   * used by toString to do some nice hex string output
   *
   * @param c
   * @return a formatted string for the passed int
   */
  private final String showHex(int c) {
    String hex = Integer.toHexString(c).toUpperCase();
    if (hex.length() == 1) {
      hex = "0" + hex + " ";
    } else {
      hex = hex + " ";
    }

    return hex;
  }

}

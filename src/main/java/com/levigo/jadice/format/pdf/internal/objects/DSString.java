package com.levigo.jadice.format.pdf.internal.objects;

import static org.jadice.util.base.Strings.utf8String;

import com.levigo.jadice.document.io.ByteUtils;
import com.levigo.jadice.format.pdf.internal.parsing.PDFDocEncoding;

public abstract class DSString extends DSObject implements Comparable<DSString> {
  private static char[] decodeUTF(byte[] rawData, boolean bigEndian) {
    rawData = ByteUtils.doShortAlignment(rawData);

    final int utfDataLength = rawData.length / 2;
    final char[] decodedData = new char[utfDataLength - 1];
    if (bigEndian) {
      // starting with 1 as the UTF BOM has to be omitted
      for (int i = 1; i < utfDataLength; i++) {
        decodedData[i - 1] = ByteUtils.getCharBigEndian(rawData, i * 2);
      }
    } else {
      // starting with 1 as the UTF BOM has to be omitted
      for (int i = 1; i < utfDataLength; i++) {
        decodedData[i - 1] = ByteUtils.getCharLittleEndian(rawData, i * 2);
      }
    }
    return decodedData;
  }

  private final byte[] rawData;

  public DSString(byte[] rawData) {
    this.rawData = rawData;
  }

  @Override
  public final boolean isString() {
    return true;
  }

  /**
   * Use this method for strings which are marked "byte string" in the PDF Specifications.
   *
   * @return raw byte values
   */
  public byte[] getRawData() {
    return rawData;
  }

  /**
   * Converts the raw bytes to ASCII/UTF-8 characters and returns the result as a {@link String}.
   * Use this method for strings which are marked "ASCII string" in the PDF Specifications.
   *
   * <br>
   * <p>
   * Note that PDF32000-1:2008 only talks about ASCII values. However, we actually treat them as
   * UTF-8, of which ASCII is a proper subset. There exist major implementations (Microsoft, Adobe,
   * Apple) which already do this. [Source: email "[PDFA-CC-tech] UTF8 within ASCII??" by Leonard
   * Rosenthol to the PDF Association's PDF/A TWG mailing list, 2013-09-13, 3h08] Therefore we need
   * to be prepared to handle documents which hold UTF-8 characters.
   *
   * @return string value, decoded using ASCII/UTF-8, as a Java {@link String}.
   */
  public String asAsciiString() {
    return utf8String(rawData);
  }

  /**
   * Converts the raw bytes to PDFDocEncoding, UTF-16BE, or UTF-16LE characters, and returns the
   * result as a {@link String}. Use this method for strings which are marked "text string" in the
   * PDF Specifications.
   *
   * <br>
   * <p>
   * Note that PDF32000-1:2008 only talks about UTF-16BE values. However, we also need to support
   * UTF-16LE. [Source: email
   * "Re: [PDFA-CC-tech] [veraPDF-tech] Unicode strings in UTF16 Little Endian format" by Leonard
   * Rosenthol to the PDF Association's PDF/A TWG mailing list, 2015-05-22, 13h18]
   *
   * @return string value, decoded using PDFDocEncoding or UTF-16, as a Java {@link String}
   */
  public String asTextString() {
    return new String(getDecodedCharArray());
  }

  private char[] getDecodedCharArray() {

    // FIXME according to current (July 2015) drafts of the PDF 2.0 spec, we might have to support
    // UTF-8 additionally in the future.

    boolean isUTF = false;
    boolean bigEndian = false;

    // check if we've got UTF data
    if (rawData != null && rawData.length >= 2) {
      // is it big endian? 0xFEFF BOM (byte order mark)
      if ((rawData[0] & 0xff) == 0xfe && (rawData[1] & 0xff) == 0xff) {
        isUTF = true;
        bigEndian = true;
      } else
        // is it little endian? 0xFFFE BOM
        if ((rawData[0] & 0xff) == 0xff && (rawData[1] & 0xff) == 0xfe) {
          isUTF = true;
          bigEndian = false;
        }
    }

    if (isUTF) {
      return decodeUTF(rawData, bigEndian);
    } else {
      return PDFDocEncoding.decode(rawData);
    }
  }

  /**
   * Instances must be compared on a byte-to-byte basis, independent of any encoding that might be
   * applied later on. Shorter keys shall appear before longer ones if they start with the same byte
   * sequence. <br>
   * Note: Name Trees seem to be the only place where PDF32000-1:2008 talks about
   * comparison/ordering of PDF string objects. Therfore we implement the requirements given there.
   */
  @Override
  public int compareTo(DSString other) {
    if (other == null) {
      // honour method contract
      throw new NullPointerException("Object to compare with must not be null.");
    }

    final int minLength = Math.min(rawData.length, other.rawData.length);
    for (int i = 0; i < minLength; i++) {
      if (rawData[i] != other.rawData[i]) {
        final int thisValue = rawData[i] & 0xFF;
        final int otherValue = other.rawData[i] & 0xFF;
        return thisValue - otherValue;
      }
    }

    // equal contents up to now. Sort shorter keys before longer keys.
    return rawData.length - other.rawData.length;
  }
}

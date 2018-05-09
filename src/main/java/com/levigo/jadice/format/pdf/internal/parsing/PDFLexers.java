package com.levigo.jadice.format.pdf.internal.parsing;

import static org.jadice.util.base.Strings.asciiBytes;

import com.levigo.jadice.format.pdf.internal.objects.DSHexString;

/**
 * Helper methods for PDF lexing
 */
public final class PDFLexers {

  /**
   * Takes a sequence of hexadecimal digits encoded as ASCII characters and decodes them to byte
   * values.
   *
   * @param digits valid hex digits as ASCII characters. Each character is represented as one byte.
   *               Valid characters are: <code>0-9</code>, <code>A-F</code>, and <code>a-f</code>.
   * @return byte representation of the given hex values
   * @throws IllegalArgumentException if:
   *                                  <ul>
   *                                  <li>the passed byte array is unbalanced, that is,
   *                                  <code>(digits.length % 2) != 0</code> evaluates to <code>true</code></li>
   *                                  <li>the passed byte array contains values that do not represent one of the valid
   *                                  hex-digit ASCII characters</li>
   *                                  </ul>
   */
  public static byte[] hexToBytes(byte[] digits) {

    // FIXME instead of throwing an exception, we could rewrite this algorithm to handle this case
    if ((digits.length % 2) != 0) {
      // unbalanced digits
      throw new IllegalArgumentException("the passed byte[] is unbalanced");
    }

    final byte[] retval = new byte[digits.length / 2];

    for (int i = 0; i < retval.length; i++) {
      int temp = 0;

      for (int j = 0; j < 2; j++) {
        // this will only affect the second run
        temp *= 16;

        final int curPos = ((i * 2) + j);

        if (digits[curPos] >= 'A' && digits[curPos] <= 'F') {
          temp += 10 + (digits[curPos] - 'A');
        } else if (digits[curPos] >= 'a' && digits[curPos] <= 'f') {
          temp += 10 + (digits[curPos] - 'a');
        } else if (digits[curPos] >= '0' && digits[curPos] <= '9') {
          temp += (digits[curPos] - '0');
        } else {
          throw new IllegalArgumentException("invalid character in sequence: " + (char) digits[curPos]);
        }
      }
      retval[i] = (byte) temp;
    }
    return retval;
  }

  /**
   * @param asciiDigits
   * @return
   * @see #hexToBytes(byte[])
   */
  public static byte[] hexToBytes(String asciiDigits) {
    final byte[] digits = asciiBytes(asciiDigits);
    final byte[] hexBytes = hexToBytes(digits);
    return hexBytes;
  }

  /**
   * @param asciiDigits
   * @return
   * @see #hexToBytes(byte[])
   */
  public static DSHexString hexToDS(String asciiDigits) {
    final byte[] decoded = hexToBytes(asciiDigits);
    final DSHexString dsHexString = new DSHexString(decoded);
    return dsHexString;
  }

  private PDFLexers() {
    // reduce visibility
  }
}

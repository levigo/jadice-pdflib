package org.jadice.util.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Utility methods for {@link String} processing.
 */
public class Strings {

  private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

  /**
   * Check whether the given {@link String} is empty or not. This method is <code>null</code>-safe.
   * <code>null</code> will be interpreted as an empty {@link String}
   *
   * @param s the {@link String} to be checked.
   * @return <code>true</code> if the {@link String} is either <code>null</code> or has zero length
   */
  public static boolean empty(String s) {
    return s == null || s.length() == 0;
  }

  /**
   * Check whether the given {@link String} is empty or not. This method basically works like
   * {@link #empty(String)} but with the addition that the {@link String} will be
   * {@link String#trim() trimmed} and checked if it contains any non whitespace character.
   *
   * @param s the {@link String} to be checked.
   * @return <code>true</code> if the {@link String} is either <code>null</code> or has zero length
   * after {@link String#trim() trimming}
   */
  public static boolean emptyTrim(String s) {
    return empty(s) || s.trim().length() == 0;
  }

  /**
   * Check whether the given {@link String} only contains {@link Character#isWhitespace(char)
   * whitespace characters}. This method is <code>null</code>-safe. <code>null</code> will be
   * interpreted as whitespace
   *
   * @param s the {@link String}
   * @return <code>true</code> if the {@link String} consists only of
   * {@link Character#isWhitespace(char) whitespace characters} or null.
   */
  public static boolean whitespace(String s) {
    if (empty(s))
      return true;

    for (final char c : s.toCharArray()) {
      if (!Character.isWhitespace(c))
        return false;
    }
    return true;
  }

  /**
   * Replace a String inside of a String.
   *
   * @param source      the String to replace stuff it
   * @param pattern     the String to replace
   * @param replacement the replacement
   * @return String the modified string
   * @author Volker Graf
   */
  public static String replaceAll(String source, String pattern, String replacement) {

    // the Buffer to write the new string in
    final StringBuilder buffer = new StringBuilder();

    final char[] chars = source.toCharArray();
    int res = 0;
    final int slen = source.length();

    // Go through all the Characters
    for (int n = 0; n < source.length(); n++) {

      // Look for Pattern !
      res = source.indexOf(pattern, n);
      if (res >= n) {
        // Pattern found !
        buffer.append(chars, n, res - n); // Add the chars before
        buffer.append(replacement); // Add the Replacement
        n = res; // We start searching from this place
      } else {
        // NO Pattern found !
        buffer.append(chars, n, slen - n);
        break;
      }
    }
    // Return replaced String
    return buffer.toString();
  }

  /**
   * Convert byte array to hex string.
   *
   * @param data byte array
   * @return hex string
   */
  public static String toHex(byte[] data) {
    return toHex(data, 0, data.length);
  }

  /**
   * Convert byte array to hex string.
   *
   * @param data   byte array
   * @param offset from offset
   * @param len    length
   * @return hex string
   */
  public static String toHex(byte[] data, int offset, int len) {
    if (data == null)
      return "'null'";

    final int end = offset + len;
    if (data.length < end)
      throw new IllegalArgumentException("Illegal offset/length/data");

    final char asHex[] = new char[len * 2];
    for (int i = offset, j = 0; i < end; i++) {
      asHex[j++] = HEX_DIGITS[data[i] >> 4 & 0x0f];
      asHex[j++] = HEX_DIGITS[data[i] & 0x0f];
    }

    return new String(asHex);
  }

  /**
   * Returns a copy of the string, with leading given parameter character omitted
   *
   * @param stringToTrim the string to trim
   * @param charToRemove the character to remove
   * @return A copy of this string with leading given character removed, or this string if it has no
   * leading character.
   */
  public static String trimLeadingCharacter(String stringToTrim, char charToRemove) {
    if (stringToTrim == null)
      return null;

    int start = 0;
    while (start < stringToTrim.length() && stringToTrim.toCharArray()[start] == charToRemove) {
      start++;
    }
    return start > 0 ? stringToTrim.substring(start, stringToTrim.length()) : stringToTrim;
  }

  /**
   * Returns a copy of the string, with trailing given parameter character omitted.
   *
   * @param stringToTrim the string to trim
   * @param charToRemove the character to remove
   * @return A copy of this string with trailing given character removed, or this string if it has
   * no trailing given character.
   */
  public static String trimTrailingCharacter(String stringToTrim, char charToRemove) {
    if (stringToTrim == null)
      return null;

    int length = stringToTrim.length();
    while (length > 0 && stringToTrim.toCharArray()[length - 1] == charToRemove) {
      length--;
    }
    return length < stringToTrim.length() ? stringToTrim.substring(0, length) : stringToTrim;
  }

  /**
   * Returns a copy of the string, with leading and trailing given parameter character omitted.
   *
   * @param stringToTrim the string to trim
   * @param charToRemove the character to remove
   * @return A copy of this string with leading and trailing given character removed, or this string
   * if it has no leading or trailing given character.
   * @author <a href="mailto:c.koehler@levigo.de">Carolin K&ouml;hler</a>
   */
  public static String trimCharacter(String stringToTrim, char charToRemove) {
    return trimTrailingCharacter(trimLeadingCharacter(stringToTrim, charToRemove), charToRemove);
  }

  /**
   * Finds the first occurrance of the given String in the given buffer.
   *
   * @param buffer      The buffer to search for the given match String.
   * @param matchString The character sequence to find in the given buffer
   * @param fromIndex   The index of the position to start the search with
   * @return -1 if the String wasn't found; the index of its first character in the buffer otherwise
   */
  public static int indexOf(byte[] buffer, String matchString, int fromIndex) {

    int matchIndex = -1;
    boolean found = false;

    for (int i = fromIndex; i < buffer.length && !found; i++) {
      matchIndex = -1;
      found = true;
      for (int j = 0; j < matchString.length(); j++) {
        // Logger.getLogger(StringTools.class).debug(
        // "checking "
        // + (i + j)
        // + ": "
        // + buffer[i
        // + j]
        // + "="
        // + (int) matchString.charAt(j));
        if (buffer[i + j] != matchString.charAt(j)) {
          found = false;
          break;
        }
      }

      matchIndex = i;
    }

    // Logger.getLogger(StringTools.class).debug(
    // "Found " + matchString + " at " + matchIndex);
    return found ? matchIndex : -1;
  }

  /**
   * Finds the last occurrance of the given String in the given buffer.
   *
   * @param buffer      The buffer to search for the given match String.
   * @param matchString The character sequence to find in the given buffer
   * @param toIndex     The index of the position to end the search with
   * @return -1 if the String wasn't found; the index of its first character in the buffer otherwise
   */
  public static int lastIndexOf(byte[] buffer, String matchString, int toIndex) {

    int matchIndex = -1;
    boolean found = false;

    for (int i = toIndex; i >= 0 && !found; i--) {
      matchIndex = -1;
      found = true;
      for (int j = 0; j < matchString.length(); j++) {
        // Logger.getLogger(StringTools.class).debug(
        // "checking "
        // + (i + j)
        // + ": "
        // + buffer[i
        // + j]
        // + "="
        // + (int) matchString.charAt(j));
        if (buffer[i + j] != matchString.charAt(j)) {
          found = false;
          break;
        }
      }

      matchIndex = i;
    }

    // Logger.getLogger(StringTools.class).debug(
    // "Found " + matchString + " at " + matchIndex);
    return found ? matchIndex : -1;
  }

  /**
   * Create a string by fully reading the supplied input stream using the default platform encoding.
   *
   * @param is
   * @return String
   * @throws IOException
   */
  public static String create(InputStream is) throws IOException {
    final ByteArrayOutputStream baos = isToBAOS(is);

    return baos.toString();
  }

  /**
   * Create a string by fully reading the supplied input stream using the specified encoding.
   *
   * @param is
   * @param encoding
   * @return String
   * @throws IOException
   */
  public static String create(InputStream is, String encoding) throws IOException {
    final ByteArrayOutputStream baos = isToBAOS(is);

    return baos.toString(encoding);
  }

  private static ByteArrayOutputStream isToBAOS(InputStream is) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final byte buffer[] = new byte[1024];

    int read;
    while ((read = is.read(buffer)) > 0)
      baos.write(buffer, 0, read);
    is.close();

    return baos;
  }

  /**
   * Checks whether the given <code>value</code> is an empty {@link String}
   *
   * @param name
   * @param value
   */
  public static void assertNotEmpty(String name, String value) {
    Objects.requireNonNull(value, name);
    if (value.length() == 0)
      throw new IllegalArgumentException(name + " cannot be empty");
  }

  /**
   * Format the string into a fixed-length representation by either trimming it to the required size
   * (if it is longer) or padding it at the end with the given padding character.
   *
   * @param s       the string
   * @param length  the desired length
   * @param padding the padding character
   * @return the result strimg which will be of the given length
   */
  public static String toFixedLength(String s, int length, char padding) {
    if (s.length() == length)
      return s;

    if (s.length() > length) {
      return s.substring(0, length);
    }

    final char chars[] = s.toCharArray();
    final char longer[] = new char[length];
    Arrays.fill(longer, padding);
    System.arraycopy(chars, 0, longer, 0, chars.length);
    return new String(longer);
  }

  /**
   * Construct a new String from the given input bytes, using ASCII encoding.
   *
   * @param bytes the bytes to construct a {@link String} from
   * @return the newly constructed String instance
   */
  public static String asciiString(byte[] bytes) {
    return constructString(bytes, "ASCII");
  }

  /**
   * Construct a new String from the given input bytes, using UTF-8 encoding.
   *
   * @param bytes the bytes to construct a {@link String} from
   * @return the newly constructed String instance
   */
  public static String utf8String(byte[] bytes) {
    return constructString(bytes, "UTF-8");
  }

  /**
   * Construct a new String from the given input bytes, using the given encoding. If the VM doesn't
   * support the given encoding, a {@link RuntimeException} will be thrown.
   * <p>
   * Note: we keep this method internal for now because of the tricky runtime exception. When
   * calling it from within this class we can make sure that the specified encoding actually exists.
   *
   * @param bytes       the bytes to construct a {@link String} from
   * @param charsetName the encoding to be used
   * @return the newly constructed String instance
   * @throws RuntimeException if the given encoding is not supported
   */
  private static String constructString(byte[] bytes, String charsetName) {
    try {
      return new String(bytes, charsetName);
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported Encoding: " + charsetName, e);
    }
  }

  /**
   * Create a byte[] representation of the given string, using ASCII encoding.
   *
   * @param s the {@link String} to generate a byte[] representation of
   * @return resulting byte[] representation of String's contents
   */
  public static byte[] asciiBytes(String s) {
    return getStringBytes(s, "ASCII");
  }

  /**
   * Create a byte[] representation of the given string, using UTF-8 encoding.
   *
   * @param s the {@link String} to generate a byte[] representation of
   * @return resulting byte[] representation of String's contents
   */
  public static byte[] utf8Bytes(String s) {
    return getStringBytes(s, "UTF-8");
  }

  /**
   * @param s the String to be encoded
   * @return a byte[] representation of the given String, encoded as UTF-16, Big Endian with BOM
   */
  public static byte[] utf16Bytes(String s) {
    return getStringBytes(s, "UTF-16");
  }

  /**
   * Construct a new byte[] from the given input String, using the given encoding. If the VM doesn't
   * support the given encoding, a {@link RuntimeException} will be thrown.
   * <p>
   * Note: we keep this method internal for now because of the tricky runtime exception. When
   * calling it from within this class we can make sure that the specified encoding actually exists.
   *
   * @param s           the {@link String} to generate a byte[] representation of
   * @param charsetName the encoding to be used
   * @return resulting byte[] representation of String's contents
   * @throws RuntimeException if the given encoding is not supported
   */
  private static byte[] getStringBytes(String s, String charsetName) {
    try {
      return s.getBytes(charsetName);
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException("Unsupported Encoding: " + charsetName, e);
    }
  }

  private Strings() {
    // disallow instantiation
  }
}

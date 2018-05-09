package com.levigo.jadice.document.io;

import java.util.Arrays;

/**
 * some helpful utils for byte handling
 */
public class ByteUtils {
  /**
   * this method converts a signed <code>byte</code> to a unsigned <code>int</code>
   *
   * @param val the <code>byte</code> value whished to get converted to an unsigned <code>int</code>
   * @return an unsigned <code>int</code>
   */
  public static final int signedByteToUnsignedInt(byte val) {
    return (val & 0xff);
  }

  /**
   * this method converts a signed <code>byte</code> to a unsigned <code>int</code>
   *
   * @param val the <code>byte</code> value whished to get converted to an unsigned <code>int</code>
   * @return an unsigned <code>int</code>
   */
  public static final int signedShortToUnsignedInt(short val) {
    return (val & 0xffff);
  }

  /**
   * this method converts a signed <code>byte</code> to a unsigned <code>int</code>
   *
   * @param val the <code>byte</code> value whished to get converted to an unsigned <code>int</code>
   * @return an unsigned <code>int</code>
   */
  public static final long signedIntToUnsignedLong(int val) {
    return (val & 0xffffffff);
  }

  /**
   * reads out a <code>boolean</code> value of the given <code>byte[]</code> at the offset specified
   * in the second parameter. A <code>boolean</code> value is being read as a single byte value
   * where any non zero value is being interpreted as true
   *
   * @param b   the <code>byte[]</code> the value is being read out
   * @param off the offset in the <code>byte[]</code> where to find the value
   * @return the <code>boolean</code> value at the given offset in the <code>byte[]</code>
   * @throws ArrayIndexOutOfBoundsException if the specified offset is out of range
   */
  public static final boolean getBoolean(byte[] b, int off) {
    return b[off] != 0;
  }

  /**
   * reads out a <code>char</code> value in the given <code>byte[]</code> at the offset specified in
   * the second parameter. The <code>char</code> will be read out using BigEndian byte ordering. A
   * <code>char</code> is being read as a double byte value.
   *
   * <br>
   * <i>ByteOrdering: BigEndian (most significant value first)</i>
   *
   * @param b   the <code>byte[]</code> the value is being read out
   * @param off the offset in the <code>byte[]</code> where to find the value
   * @return the <code>char</code> value at the given offset in the <code>byte[]</code>
   * @throws ArrayIndexOutOfBoundsException if the specified offset is out of range
   */
  public static final char getCharBigEndian(byte[] b, int off) {
    return (char) (((b[off + 1] & 0xFF) << 0) + ((b[off + 0] & 0xFF) << 8));
  }

  /**
   * reads out a <code>char</code> value in the given <code>byte[]</code> at the offset specified in
   * the second parameter. The <code>char</code> will be read out using LittleEndian byte ordering.
   * A <code>char</code> is being read as a double byte value.
   *
   * <br>
   * <i>ByteOrdering: LittleEndian (less significant value first)</i>
   *
   * @param b   the <code>byte[]</code> the value is being read out
   * @param off the offset in the <code>byte[]</code> where to find the value
   * @return the <code>char</code> value at the given offset in the <code>byte[]</code>
   * @throws ArrayIndexOutOfBoundsException if the specified offset is out of range
   */
  public static final char getCharLittleEndian(byte[] b, int off) {
    return (char) (((b[off + 0] & 0xFF) << 0) + ((b[off + 1] & 0xFF) << 8));
  }

  /**
   * reads out a <code>short</code> value in the given <code>byte[]</code> at the offset specified
   * in the second parameter. The <code>short</code> will be read out using BigEndian byte ordering.
   * A <code>short</code> is being read as a double byte value.
   *
   * <br>
   * <i>ByteOrdering: BigEndian (most significant value first)</i>
   *
   * @param b   the <code>byte[]</code> the value is being read out
   * @param off the offset in the <code>byte[]</code> where to find the value
   * @return the <code>short</code> value at the given offset in the <code>byte[]</code>
   * @throws ArrayIndexOutOfBoundsException if the specified offset and the length is out of
   *                                        range
   */
  public static final short getShort(byte[] b, int off) {
    return (short) (((b[off + 1] & 0xFF) << 0) + ((b[off + 0] & 0xFF) << 8));
  }

  /**
   * reads out a <code>int</code> value in the given <code>byte[]</code> at the offset specified in
   * the second parameter. The <code>int</code> will be read out using BigEndian byte ordering. A
   * <code>int</code> is being read as a four byte value.
   *
   * <br>
   * <i>ByteOrdering: BigEndian (most significant value first)</i>
   *
   * @param b   the <code>byte[]</code> the value is being read out
   * @param off the offset in the <code>byte[]</code> where to find the value
   * @return the <code>int</code> value at the given offset in the <code>byte[]</code>
   * @throws ArrayIndexOutOfBoundsException if the specified offset and the length is out of
   *                                        range
   */
  public static final int getInt(byte[] b, int off) {
    return ((b[off + 3] & 0xFF) << 0) + ((b[off + 2] & 0xFF) << 8) + ((b[off + 1] & 0xFF) << 16) + ((b[off + 0] & 0xFF)
        << 24);
  }

  /**
   * reads out a <code>float</code> value in the given <code>byte[]</code> at the offset specified
   * in the second parameter. The <code>float</code> will be read out using BigEndian byte ordering.
   * A <code>float</code> is being read as a four byte value.
   *
   * <br>
   * <i>ByteOrdering: BigEndian (most significant value first)</i>
   *
   * @param b   the <code>byte[]</code> the value is being read out
   * @param off the offset in the <code>byte[]</code> where to find the value
   * @return the <code>float</code> value at the given offset in the <code>byte[]</code>
   * @throws ArrayIndexOutOfBoundsException if the specified offset and the length is out of
   *                                        range
   */
  public static final float getFloat(byte[] b, int off) {
    final int i =
        ((b[off + 3] & 0xFF) << 0) + ((b[off + 2] & 0xFF) << 8) + ((b[off + 1] & 0xFF) << 16) + ((b[off + 0] & 0xFF)
            << 24);
    return Float.intBitsToFloat(i);
  }

  /**
   * reads out a <code>long</code> value in the given <code>byte[]</code> at the offset specified in
   * the second parameter. The <code>long</code> will be read out using BigEndian byte ordering. A
   * <code>long</code> is being read as a eight byte value.
   *
   * <br>
   * <i>ByteOrdering: BigEndian (most significant value first)</i>
   *
   * @param b   the <code>byte[]</code> the value is being read out
   * @param off the offset in the <code>byte[]</code> where to find the value
   * @return the <code>long</code> value at the given offset in the <code>byte[]</code>
   * @throws ArrayIndexOutOfBoundsException if the specified offset and the length is out of
   *                                        range
   */
  public static final long getLong(byte[] b, int off) {
    return ((b[off + 7] & 0xFFL) << 0) + ((b[off + 6] & 0xFFL) << 8) + ((b[off + 5] & 0xFFL) << 16) + (
        (b[off + 4] & 0xFFL) << 24) + ((b[off + 3] & 0xFFL) << 32) + ((b[off + 2] & 0xFFL) << 40) + (
        (b[off + 1] & 0xFFL) << 48) + ((b[off + 0] & 0xFFL) << 56);
  }

  /**
   * reads out a <code>double</code> value in the given <code>byte[]</code> at the offset specified
   * in the second parameter. The <code>double</code> will be read out using BigEndian byte
   * ordering. A <code>double</code> is being read as a eight byte value.
   *
   * <br>
   * <i>ByteOrdering: BigEndian (most significant value first)</i>
   *
   * @param b   the <code>byte[]</code> the value is being read out
   * @param off the offset in the <code>byte[]</code> where to find the value
   * @return the <code>double</code> value at the given offset in the <code>byte[]</code>
   * @throws ArrayIndexOutOfBoundsException if the specified offset and the length is out of
   *                                        range
   */
  public static final double getDouble(byte[] b, int off) {
    final long j =
        ((b[off + 7] & 0xFFL) << 0) + ((b[off + 6] & 0xFFL) << 8) + ((b[off + 5] & 0xFFL) << 16) + ((b[off + 4] & 0xFFL)
            << 24) + ((b[off + 3] & 0xFFL) << 32) + ((b[off + 2] & 0xFFL) << 40) + ((b[off + 1] & 0xFFL) << 48) + (
            (b[off + 0] & 0xFFL) << 56);
    return Double.longBitsToDouble(j);
  }

  /**
   * writes a <code>boolean</code> value to the given <code>byte[]</code> at the offset specified in
   * the second parameter. A <code>boolean</code> value is being written as a single byte value.
   *
   * @param b   the <code>byte[]</code> the value is being read out
   * @param off the offset in the <code>byte[]</code> where to find the value
   * @throws ArrayIndexOutOfBoundsException if the specified offset is out of range
   */
  public static final void putBoolean(byte[] b, int off, boolean val) {
    b[off] = (byte) (val ? 1 : 0);
  }

  /**
   * writes a <code>char</code> value to the given <code>byte[]</code> at the offset specified in
   * the second parameter. The <code>char</code> will be written using BigEndian byte ordering. A
   * <code>char</code> is being written as a two byte value.
   *
   * <br>
   * <i>ByteOrdering: BigEndian (most significant value first)</i>
   *
   * @param b   the <code>byte[]</code> the value is being written to
   * @param off the offset in the <code>byte[]</code> where to write the value
   * @param val the value to be written
   * @throws ArrayIndexOutOfBoundsException if the specified offset and the length is out of
   *                                        range
   */
  public static final void putChar(byte[] b, int off, char val) {
    b[off + 1] = (byte) (val >>> 0);
    b[off + 0] = (byte) (val >>> 8);
  }

  /**
   * writes a <code>short</code> value to the given <code>byte[]</code> at the offset specified in
   * the second parameter. The <code>short</code> will be written using BigEndian byte ordering. A
   * <code>short</code> is being written as a two byte value.
   *
   * <br>
   * <i>ByteOrdering: BigEndian (most significant value first)</i>
   *
   * @param b   the <code>byte[]</code> the value is being written to
   * @param off the offset in the <code>byte[]</code> where to write the value
   * @param val the value to be written
   * @throws ArrayIndexOutOfBoundsException if the specified offset and the length is out of
   *                                        range
   */
  public static final void putShort(byte[] b, int off, short val) {
    b[off + 1] = (byte) (val >>> 0);
    b[off + 0] = (byte) (val >>> 8);
  }

  /**
   * writes a <code>int</code> value to the given <code>byte[]</code> at the offset specified in the
   * second parameter. The <code>int</code> will be written using BigEndian byte ordering. A
   * <code>int</code> is being written as a four byte value.
   *
   * <br>
   * <i>ByteOrdering: BigEndian (most significant value first)</i>
   *
   * @param b   the <code>byte[]</code> the value is being written to
   * @param off the offset in the <code>byte[]</code> where to write the value
   * @param val the value to be written
   * @throws ArrayIndexOutOfBoundsException if the specified offset and the length is out of
   *                                        range
   */
  public static final void putInt(byte[] b, int off, int val) {
    b[off + 3] = (byte) (val >>> 0);
    b[off + 2] = (byte) (val >>> 8);
    b[off + 1] = (byte) (val >>> 16);
    b[off + 0] = (byte) (val >>> 24);
  }

  /**
   * writes a <code>float</code> value to the given <code>byte[]</code> at the offset specified in
   * the second parameter. The <code>float</code> will be written using BigEndian byte ordering. A
   * <code>float</code> is being written as a four byte value.
   *
   * <br>
   * <i>ByteOrdering: BigEndian (most significant value first)</i>
   *
   * @param b   the <code>byte[]</code> the value is being written to
   * @param off the offset in the <code>byte[]</code> where to write the value
   * @param val the value to be written
   * @throws ArrayIndexOutOfBoundsException if the specified offset and the length is out of
   *                                        range
   */
  public static final void putFloat(byte[] b, int off, float val) {
    final int i = Float.floatToIntBits(val);
    b[off + 3] = (byte) (i >>> 0);
    b[off + 2] = (byte) (i >>> 8);
    b[off + 1] = (byte) (i >>> 16);
    b[off + 0] = (byte) (i >>> 24);
  }

  /**
   * writes a <code>long</code> value to the given <code>byte[]</code> at the offset specified in
   * the second parameter. The <code>long</code> will be written using BigEndian byte ordering. A
   * <code>long</code> is being written as a eight byte value.
   *
   * <br>
   * <i>ByteOrdering: BigEndian (most significant value first)</i>
   *
   * @param b   the <code>byte[]</code> the value is being written to
   * @param off the offset in the <code>byte[]</code> where to write the value
   * @param val the value to be written
   * @throws ArrayIndexOutOfBoundsException if the specified offset and the length is out of
   *                                        range
   */
  public static final void putLong(byte[] b, int off, long val) {
    b[off + 7] = (byte) (val >>> 0);
    b[off + 6] = (byte) (val >>> 8);
    b[off + 5] = (byte) (val >>> 16);
    b[off + 4] = (byte) (val >>> 24);
    b[off + 3] = (byte) (val >>> 32);
    b[off + 2] = (byte) (val >>> 40);
    b[off + 1] = (byte) (val >>> 48);
    b[off + 0] = (byte) (val >>> 56);
  }

  /**
   * writes a <code>double</code> value to the given <code>byte[]</code> at the offset specified in
   * the second parameter. The <code>double</code> will be written using BigEndian byte ordering. A
   * <code>double</code> is being written as a eight byte value.
   *
   * <br>
   * <i>ByteOrdering: BigEndian (most significant value first)</i>
   *
   * @param b   the <code>byte[]</code> the value is being written to
   * @param off the offset in the <code>byte[]</code> where to write the value
   * @param val the value to be written
   * @throws ArrayIndexOutOfBoundsException if the specified offset and the length is out of
   *                                        range
   */
  public static final void putDouble(byte[] b, int off, double val) {
    final long j = Double.doubleToLongBits(val);
    b[off + 7] = (byte) (j >>> 0);
    b[off + 6] = (byte) (j >>> 8);
    b[off + 5] = (byte) (j >>> 16);
    b[off + 4] = (byte) (j >>> 24);
    b[off + 3] = (byte) (j >>> 32);
    b[off + 2] = (byte) (j >>> 40);
    b[off + 1] = (byte) (j >>> 48);
    b[off + 0] = (byte) (j >>> 56);
  }

  /**
   * this method checks if the length of the passed <code>byte[]</code> is a multiple of 2. If not
   * it will create a new byte[] copy the source data to the new, append <code>0</code> and return
   * the resulting byte[]
   *
   * @param source the <code>byte[]</code> to be short aligned
   * @return a <code>byte[]</code> which is short aligned
   */
  public static final byte[] doShortAlignment(byte[] source) {
    if ((source.length & 0x1) == 0) {
      return source;
    } else {
      return Arrays.copyOf(source, source.length + 1);
    }
  }
}

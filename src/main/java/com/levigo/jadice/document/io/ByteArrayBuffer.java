package com.levigo.jadice.document.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * A <code>ByteArrayBuffer</code> buffers bytes but is not bound to a fixed
 * capacity like arrays.
 */
public class ByteArrayBuffer {
  /**
   * Total number of bytes.
   */
  protected int totalSize;
  /**
   * Number of bytes in the tail buffer.
   */
  protected int count;

  /**
   * The the last tail buffer.
   */
  protected byte buf[];

  /**
   * A list of already filled up byte arrays.
   */
  protected List<byte[]> buffers = new ArrayList<>();

  /**
   * Creates new ByteArrayBuffer with default capacity of 4096 bytes
   */
  public ByteArrayBuffer() {
    this(4096);
  }

  /**
   * Creates a byte buffer with a certain capacity.
   *
   * @param capacity the initial capacity
   */
  public ByteArrayBuffer(int capacity) {
    if (capacity < 1)
      capacity = 4096;
    buf = new byte[capacity];
    buffers.add(buf);
  }

  /**
   * Appends a subarray of the given byte array. The buffer will grow by len
   * bytes.
   *
   * @param b   the array to be appended
   * @param off the offset to the start of the array
   * @param len the length of bytes to append
   */
  public synchronized void append(byte b[], int off, int len) {
    if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0)
      throw new IllegalArgumentException("Invalid append parameters");

    int newcount = count + len;
    int diff = len;
    while (newcount > buf.length) {
      final int oldBufLen = buf.length - count;
      System.arraycopy(b, off, buf, count, oldBufLen);

      buf = new byte[buf.length];
      // buf = new byte[buf.length * 2];
      buffers.add(buf);
      count = 0;

      diff -= oldBufLen;
      off += oldBufLen;
      newcount = diff;
    }

    System.arraycopy(b, off, buf, count, diff);
    count = newcount;
    totalSize += len;
  }

  /**
   * Appends the given array of bytes.
   *
   * @param b the array to be appended
   */
  public synchronized void append(byte b[]) {
    append(b, 0, b.length);
  }

  /**
   * Appends a character as byte to the buffer.
   *
   * @param c the character to be appended
   */
  public synchronized void append(char c) {
    append((byte) c);
  }

  /**
   * Appends another ByteArrayBuffer to this buffer.
   *
   * @param buf the <CODE>ByteArrayBuffer</CODE> to be appended
   */
  public synchronized void append(ByteArrayBuffer buf) {
    if (buf == null)
      return;
    append(buf.buf, 0, buf.count);
  }

  /**
   * Appends a byte to this buffer
   *
   * @param b the byte to be appended
   */
  public synchronized void append(byte b) {
    if (count == buf.length) {
      buf = new byte[buf.length];
      // buf = new byte[buf.length * 2];
      buffers.add(buf);
      count = 0;
    }
    buf[count] = b;
    count++;
    totalSize++;
  }

  /**
   * Returns the byte value at the given index
   *
   * @param index the index of the byte value looked for
   * @return byte the byte value at index position
   * @throws ArrayIndexOutOfBoundsException if index is smaller than 0 or
   *                                        greater then the current buffer size
   */
  public synchronized byte getByteAt(int index) {
    final int size = size();
    if (index < 0 || index >= size)
      throw new ArrayIndexOutOfBoundsException("ByteArrayBuffer count =" + size + " index=" + index);
    final int bufferedSize = Math.max(0, size - count);
    if (bufferedSize > index) {
      final int start = index / buf.length;
      int counter = start * buf.length;
      for (int i = start; i < buffers.size(); i++) {
        final byte buffer[] = buffers.get(i);
        final int length = buffer != buf ? buffer.length : count;
        if (counter + length > index)
          return buffer[index - counter];

        counter += length;
      }
    } else
      return buf[index - bufferedSize];

    return -1;
  }

  /**
   * Fills up to <code>len</code> bytes from the buffer, and stores them into
   * <code>b</code> starting at index <code>off</code>.
   *
   * @param index the index of the byte values looked for
   * @param b     the byte array to fill up
   * @param off   the starting position within <code>b</code> to fill up to.
   * @param len   the maximum number of bytes to fill in.
   * @return the number of bytes actually filled into <code>b</code>
   * @throws ArrayIndexOutOfBoundsException if <code>index</code> is smaller
   *                                        than 0 or greater then the current buffer size or if
   *                                        <code>off</code> is negative, <code>len</code> is negative,
   *                                        or <code>off +
   *                                        len</code> is greater than <code>b.length</code>.
   */
  public synchronized int getBytesAt(int index, byte[] b, int off, int len) {
    if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
      throw new ArrayIndexOutOfBoundsException("off < 0 || len < 0 || off + len > b.length!");
    }
    if (index < 0 || index >= totalSize)
      throw new ArrayIndexOutOfBoundsException("ByteArrayBuffer count =" + totalSize + " index=" + index);

    int copyCount = 0;
    final int bufferedSize = count > totalSize ? 0 : totalSize - count;

    if (bufferedSize > index) {
      final int start = index / buf.length;
      int counter = start * buf.length;
      final int buffersCount = buffers.size();
      for (int i = start; i < buffersCount && copyCount < len; i++) {
        final byte buffer[] = buffers.get(i);
        final int length = buffer != buf ? buffer.length : count;
        if (counter + length > index) {
          final int toCopy = Math.min(length - index + counter - copyCount, len - copyCount);
          System.arraycopy(buffer, index - counter + copyCount, b, off + copyCount, toCopy);
          copyCount += toCopy;
        }
        counter += length;
      }
    } else {
      copyCount = Math.min(len, count - index + bufferedSize);
      System.arraycopy(buf, index - bufferedSize, b, off, copyCount);
    }

    return copyCount;
  }

  /**
   * Changes the byte value at given index
   *
   * @param index    the index of the byte value looked for
   * @param newValue the new byte value at index position
   * @throws ArrayIndexOutOfBoundsException if index is smaller than 0 or
   *                                        greater then the current buffer size
   */
  public synchronized void setByteAt(int index, byte newValue) {
    final int size = size();
    if (index < 0 || index >= size)
      throw new ArrayIndexOutOfBoundsException("ByteArrayBuffer count =" + size + " index=" + index);
    final int bufferedSize = Math.max(0, size - count);
    if (bufferedSize > index) {
      int counter = 0;
      for (final byte[] buffer : buffers) {
        final int length = buffer != buf ? buffer.length : count;
        if (counter + length > index) {
          buffer[index - counter] = newValue;
          break;
        }

        counter += length;
      }

    } else
      buf[index - bufferedSize] = newValue;
  }

  /**
   * Clears the buffer
   */
  public synchronized void clear() {
    count = 0;
    totalSize = 0;
    buffers.clear();
    buf = new byte[buf.length];
    buffers.add(buf);
  }

  /**
   * Creates a newly allocated byte array. Its size is the current size of this
   * output stream and the valid contents of the buffer have been copied into
   * it.
   *
   * @return the current contents of this output stream, as a byte array.
   */
  public synchronized byte[] toByteArray() {
    final int totalSize = size();

    final byte all[] = new byte[totalSize];
    int pos = 0;
    for (final byte[] buffer : buffers) {
      // be careful to count only count bytes for the last buffer in the
      // list.
      if (buffer != buf) {
        System.arraycopy(buffer, 0, all, pos, buffer.length);
        pos += buffer.length;
      } else {
        System.arraycopy(buffer, 0, all, pos, count);
        pos += count;
      }
    }

    return all;
  }

  /**
   * Returns the current size of the buffer.
   *
   * @return the value of the the total size of bytes contained in this buffer
   */
  public synchronized int size() {
    return totalSize;
  }

  /**
   * Converts the buffer's contents into a string, translating bytes into
   * characters according to the platform's default character encoding.
   *
   * @return String translated from the buffer's contents.
   */
  @Override
  public String toString() {
    return new String(toByteArray());
  }

  /**
   * Converts the buffer's contents into a string, translating bytes into
   * characters according to the specified character encoding.
   *
   * @param enc a character-encoding name.
   * @return String translated from the buffer's contents.
   * @throws UnsupportedEncodingException If the named encoding is not
   *                                      supported.
   */
  public String toString(String enc) throws UnsupportedEncodingException {
    return new String(toByteArray(), enc);
  }

  /**
   * Writes the complete contents of this byte buffer output to the specified
   * output stream argument, as if by calling the output stream's write method
   * using <code>out.write(buf, 0, count)</code>.
   *
   * @param out the output stream to which to write the data.
   * @throws IOException if an I/O error occurs.
   */
  public void writeTo(OutputStream out) throws IOException {
    out.write(toByteArray());
    out.flush();
  }
}

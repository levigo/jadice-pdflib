package com.levigo.jadice.document.internal.codec;

import java.io.IOException;
import java.io.InputStream;

import com.levigo.jadice.document.io.SeekableInputStream;

/**
 * <code>ASCII85InputStream</code> is an {@link java.io.FilterInputStream} (which has the super
 * class {@link java.io.InputStream}) and does on the fly ASCII85 decoding. Handling is identical
 * with any other {@link java.io.InputStream} except that the only source is only another
 * {@link java.io.InputStream}
 */
// FIXME: in some cases (I don't know when and how) there are errors
// at the end of decoding (in the cases that I know there are
// missing 0x00 characters at the end
public class ASCII85InputStream extends SeekableInputStream {

  private final SeekableInputStream stream;
  /**
   * <code>decodedBuffer</code> will contain an array of 4 int with already decoded data. The values
   * will be returned by <code>read()</code>. The current position in the buffer is stored in
   * <code>resultBufferPos</code>
   *
   * @see #decBufPos
   * @see #read()
   */
  int[] decBuf = new int[4];
  /**
   * <code>resultBufferPos</code> holds the current position in the buffer of decoded data (
   * <code>resultBuffer</code>).<br>
   * the initial value is <code>decodedBuffer.length</code> to force the filling of the buffer
   *
   * @see #decBuf
   */
  int decBufPos = decBuf.length;
  int decBufSize = 0;
  private long streamPos;
  private boolean eof = false;

  /**
   * construct an "on-the-fly" ASCII85 filter input stream. decoding will be done when invoking
   * {@link #read()}, {@link #read(byte[])} or {@link #read(byte[], int, int)}
   *
   * @param stream the source stream
   */
  public ASCII85InputStream(SeekableInputStream stream) {
    this.stream = stream;
  }

  /**
   * Decode the passed int[] into the destination array. The int[] array has to be of size 5. The
   * destination array must be of size 4.
   *
   * @param toDecode the int[5] of data to decode
   * @param dst      the int[4] into which to decode
   */
  private void decode(int[] toDecode, int[] dst) {
    long tempValue = 0;
    for (int i = 0; i < 5; ++i) {
      tempValue = (tempValue * 85) + (toDecode[i] - '!');
    }

    // place the decoded data into the result array
    for (int i = 0; i < 4; ++i) {
      dst[i] = (int) ((tempValue >> ((3 - i) * 8)) & 0x000000ff);
    }
  }

  /**
   * this <code>read()</code> implementation behaves like {@link InputStream#read()} except that
   * there is on the fly ASCII85 decoding.
   *
   * @see java.io.FilterInputStream#read()
   */
  @Override
  public int read() throws IOException {

    // if (eof == true && this.lastGroupSize >= 0) {
    // if (decBufPos <= lastGroupSize)
    // return decBuf[decBufPos++];
    // else
    // return -1;
    // }

    if (decBufPos >= decBufSize) {

      if (eof == true) {
        return -1;
      }

      final int[] buf = new int[5];

      int numRead = 0;

      int c = 0;

      decBufSize = 4;

      // read until either an EOF is read or the buffer is filled
      while (!eof && c >= 0 && numRead < 5) {
        c = stream.read();

        if ((c == 10) // Newline
            || (c == 13) // carriage return
            || (c == 32)) { // whitespace

          // this token is a whitespace. ASCII85 ignores whitespaces
          continue;

        } else if (c == 'z') {
          if (numRead > 0) {
            throw new IOException("the encoded stream (ASCII85) is corrupt. There is a 'z' in a Group of five");
          }

          buf[0] = buf[1] = buf[2] = buf[3] = buf[4] = 0x21;
          numRead = 5;

        } else if (c == 0x7e) { // represents: ~
          /*
           * End of ASCII85 Stream data. this Part fixes the bugs #1323 and #2235 As the stream now
           * returns only the last bytes that have been compressed
           */
          buf[numRead++] = 0x7e;
          eof = true;

          if (numRead > 1 && numRead <= 5) {
            /*
             * decreasing decBufSize by 2 as the last numRead increment was the read of the ~ sign
             * which hasn't to be counted
             */
            decBufSize = numRead - 2;

            for (int i = numRead; i < 5; i++) {

              buf[i] = 0x21; // represents: !

            }

            numRead = 5;
          } else {
            return -1;
          }
        } else {
          // check for consitency
          if (c < '!' || c > 'u') {
            throw new IOException("corrupt ASCII85 data. Cause: byte out of range: " + c);
          }
          buf[numRead++] = c;
        }
      }
      decode(buf, decBuf);
      decBufPos = 0;
    }
    bitOffset = 0;
    streamPos++;

    return decBuf[decBufPos++];
  }

  /**
   * @see java.io.InputStream#read(byte[], int, int)
   */
  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    } else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return 0;
    }

    // FIXME this implementation is very slow. we should try to directly
    // access
    // the data in the decoded buffer
    int i = 0;
    for (; i < len; i++) {
      final int c = read();
      if (c >= 0) {
        b[off + i] = (byte) c;
      } else if (i == 0) {
        return -1;
      } else {
        return i;
      }
    }

    return i;
  }

  /**
   * @see java.io.InputStream#read(byte[])
   */
  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public long getStreamPosition() throws IOException {
    checkClosed();
    return streamPos;
  }

  @Override
  public long length() throws IOException {
    return -1L;
  }

  /**
   * Sets the current stream position, measured from the beginning of this data stream, at which the
   * next read occurs. The offset may be set beyond the end of this data stream. Setting the offset
   * beyond the end does not change the data length, an <code>EOFException</code> will be thrown
   * only if a read is performed. The bit offset is set to 0.
   * <p>
   * <p>
   * An <code>IndexOutOfBoundsException</code> will be thrown if <code>pos</code> is smaller than
   * the flushed position (as returned by <code>getflushedPosition</code>).
   * <p>
   * <p>
   * It is legal to seek past the end of the file; an <code>EOFException</code> will be thrown only
   * if a read is performed.
   *
   * @param pos a <code>long</code> containing the desired file pointer position.
   * @throws IndexOutOfBoundsException if <code>pos</code> is smaller than the flushed position.
   * @throws IOException               if any other I/O error occurs.
   */
  @Override
  public void seek(long pos) throws IOException {
    checkClosed();

    if (pos < flushedPos) {
      throw new IndexOutOfBoundsException("pos < flushedPos!");
    }
    bitOffset = 0;

    long i = 0;
    if (pos < streamPos) {
      stream.seek(0);
      streamPos = 0;
      decBufPos = 0;
      decBufSize = 0;
      eof = false;
    } else {
      i = streamPos;
    }

    for (; i < pos; i++) {
      read();
    }
    streamPos = pos;
  }

  @Override
  public long getSizeEstimate() {
    return decBuf.length + stream.getSizeEstimate();
  }
}

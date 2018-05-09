package com.levigo.jadice.document.internal.codec;

import java.io.IOException;
import java.io.InputStream;

import com.levigo.jadice.document.io.SeekableInputStream;

/**
 * <code>ASCIIHexInputStream</code> is an on the fly ASCII hex decoder. ASCII-Hex is a really simple
 * format which simply consists of Hex pairs, each identifying one byte value.
 * <code>ASCIIHexInputStream</code> behaves like any other {@link InputStream}
 */
public class ASCIIHexInputStream extends SeekableInputStream {

  private final SeekableInputStream stream;
  private long streamPos;
  private boolean atEOF;

  /**
   * @param stream the source
   */
  public ASCIIHexInputStream(SeekableInputStream stream) {
    this.stream = stream;
  }

  /**
   * construct an "on-the-fly" ASCII-Hex filter input stream. decoding will be done when invoking
   * {@link #read()}, {@link #read(byte[])} or {@link #read(byte[], int, int)}
   *
   * @see java.io.FilterInputStream#read()
   */
  @Override
  public int read() throws IOException {
    final int hi = fetchNibble();

    // if highByte already is EOF return EOF
    if (hi < 0)
      return -1;

    int lo = fetchNibble();

    // if lowByte is EOF, but we need it for decoding set it to 0
    if (lo < 0)
      lo = 0;

    streamPos++;
    return (hi << 4) | lo;
  }

  private final int fetchNibble() throws IOException {
    if (atEOF)
      return -1;

    do {
      final int r = stream.read();
      switch (r){
        case 0x0:
        case ' ':
        case '\r':
        case '\n':
        case '\t':
          continue; // skip whitespace

        case '>':
        case -1:
          // Remember EOF condition, so that subsequent reads don't read beyond it.
          atEOF = true;
          return -1;

        default:
          if ((r >= '0' && r <= '9'))
            return r - '0';
          if ((r >= 'a' && r <= 'f'))
            return r - 'a' + 0xa;
          if ((r >= 'A' && r <= 'F'))
            return r - 'A' + 0xa;

          throw new IOException(
              "Invalid character in ASCIIHexDecode stream '" + ((char) r) + "' (" + Integer.toString(r) + ") at " + (
                  stream.getStreamPosition() - 1));
      }
    } while (true);
  }

  /**
   * @see java.io.InputStream#read(byte[])
   */
  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
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

    int c = read();
    if (c == -1) {
      return -1;
    }
    b[off] = (byte) c;

    int i = 1;

    // catch the IOException, return the already read values
    try {
      for (; i < len; i++) {
        c = read();
        if (c == -1) {
          break;
        }

        b[off + i] = (byte) c;
      }
    } catch (final IOException ee) {
      // just return the alread read values
    }
    return i;
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

  @Override
  public void seek(long pos) throws IOException {
    checkClosed();

    if (pos < flushedPos) {
      throw new IndexOutOfBoundsException("pos < flushedPos!");
    }

    bitOffset = 0;
    if (pos < streamPos) {
      // backwards seek -> we have to start all over
      stream.seek(0);
      streamPos = 0;
      atEOF = false;
    }

    while (streamPos < pos)
      if (read() < 0)
        break;

    streamPos = pos;
  }

  @Override
  public long getSizeEstimate() {
    return stream.getSizeEstimate();
  }

}

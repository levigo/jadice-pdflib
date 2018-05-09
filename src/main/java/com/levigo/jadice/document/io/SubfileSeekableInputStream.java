package com.levigo.jadice.document.io;

import static java.lang.Math.min;

import java.io.IOException;

/**
 * A wrapper for a SeekableInputStream which is able to provide a view of a specific part of the
 * wrapped stream. Read accesses to the wrapped stream are synchronized, so that users of this
 * stream need to deal with synchronization against other users of the same instance, but not
 * against other users of the wrapped stream.
 */
public class SubfileSeekableInputStream extends SeekableInputStream {

  /**
   * The source stream to be read from
   */
  private final SeekableInputStream source;
  /**
   * The start position of the data portion
   */
  private final long startPos;

  /**
   * The length of the partial stream to be read. will be set by the constructor.
   */
  private final long length;
  private final boolean propagateClose;
  /**
   * The current position in partial stream
   */
  private long position = 0;
  /**
   * The read-ahead buffer
   */
  private byte buffer[];

  private long bufferBase = Long.MIN_VALUE;

  private long bufferTop = Long.MIN_VALUE;

  /**
   * Creates a seekable stream part access
   *
   * @param stream the {@link SeekableInputStream}
   * @param start  the start offset of the partial view
   * @param length the length of the partial view to be read
   */
  public SubfileSeekableInputStream(SeekableInputStream stream, long start, long length) {
    this(stream, start, length, false);
    setReadAhead(64);
  }

  /**
   * Creates a seekable stream part access
   *
   * @param stream         the {@link SeekableInputStream}
   * @param start          the start offset of the partial view
   * @param length         the length of the partial view to be read
   * @param propagateClose propagate close to wrapped stream
   */
  public SubfileSeekableInputStream(SeekableInputStream stream, long start, long length, boolean propagateClose) {
    source = stream;
    this.length = length;
    this.propagateClose = propagateClose;

    // inherit the parent's byte order
    byteOrder = stream.getByteOrder();

    startPos = start;

    setReadAhead((int) Math.min(64L, length));
  }

  /**
   * @see com.levigo.jadice.document.io.SeekableInputStream#getStreamPosition()
   */
  @Override
  public long getStreamPosition() throws IOException {
    checkClosed();
    return position;
  }

  /**
   * @see java.io.InputStream#close()
   */
  @Override
  public void close() throws IOException {
    super.close();
    if (propagateClose)
      source.close();
    buffer = null;
  }

  /**
   * @see java.io.InputStream#read()
   */
  @Override
  public int read() throws IOException {
    checkClosed();

    final long remaining = length - position;
    if (remaining < 1)
      return -1;

    // can we satisfy the request from the read-ahead buffer?
    if (position >= bufferBase && position < bufferTop)
      return buffer[(int) (position++ - bufferBase)] & 0xff;

    bitOffset = 0;
    synchronized (source) {
      final long sourcePosition = startPos + position; // position in source stream
      if (sourcePosition != source.getStreamPosition())
        source.seek(sourcePosition);

      // fill the read-ahead buffer, unless there is nothing left or even the original
      // request could not be satisfied at this time.
      if (remaining > 0) {
        final int r = source.read(buffer, 0, (int) min(buffer.length, remaining));
        if (r > 0) {
          bufferBase = position;
          bufferTop = position + r;
          position++;
          return buffer[0] & 0xff;
        }
      }
    }

    return -1;
  }

  /**
   * @see java.io.InputStream#read(byte[], int, int)
   */
  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    checkClosed();
    bitOffset = 0;

    long remaining = length - position;
    if (remaining <= 0)
      return -1;

    if (len > remaining)
      len = (int) remaining;

    // can we satisfy the request (or at least some of it) from the read-ahead buffer?
    int read = 0;
    if (position >= bufferBase && position < bufferTop) {
      read = (int) min(bufferTop - position, len);
      System.arraycopy(buffer, (int) (position - bufferBase), b, off, read);
      position += read;

      // all of it?
      if (read == len)
        return read;

      remaining -= read;
      len -= read;
      off += read;
    }

    synchronized (source) {
      final long sourcePosition = startPos + position; // position in source stream
      if (sourcePosition != source.getStreamPosition())
        source.seek(sourcePosition);

      // first satisfy the destination buffer
      int r = source.read(b, off, (int) min(len, remaining));
      if (r > 0) {
        position += r;
        remaining -= r;
        read += r;

        // then fill the read-ahead buffer, unless there is nothing left or even the original
        // request could not be satisfied at this time.
        if (read >= len && remaining > 0) {
          r = source.read(buffer, 0, (int) min(buffer.length, remaining));
          if (r > 0) {
            bufferBase = position;
            bufferTop = bufferBase + r;
          }
        }
      } else if (read == 0)
        return -1;
    }

    return read;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#length()
   */
  @Override
  public long length() throws IOException {
    return length;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#seek(long)
   */
  @Override
  public void seek(long pos) throws IOException {
    checkClosed();

    if (pos < flushedPos)
      throw new IndexOutOfBoundsException("pos < flushedPos!");

    bitOffset = 0;
    position = pos;
  }

  /**
   * Boolean flag indicating whether if this stream is closed the closing of the warpped stream
   * should be forced.
   *
   * @return boolean flag
   */
  public boolean doPropagateClose() {
    return propagateClose;
  }

  @Override
  public long getSizeEstimate() {
    return length > 0 && length != Integer.MAX_VALUE ? (int) length : source != null ? source.getSizeEstimate() : 1024;
  }

  /**
   * @return the readAhead
   */
  public int getReadAhead() {
    return buffer.length;
  }

  /**
   * @param readAhead the readAhead to set
   */
  public void setReadAhead(int readAhead) {
    if (readAhead < 0)
      throw new IllegalArgumentException("read-ahead must be >= 0");

    buffer = new byte[readAhead];
  }
}

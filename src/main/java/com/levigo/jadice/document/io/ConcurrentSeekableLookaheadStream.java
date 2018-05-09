package com.levigo.jadice.document.io;

import java.io.IOException;

/**
 * the {@link ConcurrentSeekableLookaheadStream} is specialized {@link SeekableLookaheadStream} with
 * synchronized stream access. This implementation will synchronize using the stream as the mutex.
 * Concurrent stream access will be handled by seeking to the last read position if necessary.
 */
public class ConcurrentSeekableLookaheadStream extends SeekableLookaheadStream {

  private long streamPosition;

  public ConcurrentSeekableLookaheadStream(SeekableInputStream source, int maxLookahead, long initialStreamPosition) {
    super(source, maxLookahead);
    streamPosition = initialStreamPosition;
  }

  public ConcurrentSeekableLookaheadStream(SeekableInputStream source, int maxLookahead) {
    this(source, maxLookahead, 0L);
  }

  public ConcurrentSeekableLookaheadStream(SeekableInputStream source) {
    this(source, DEFAULT_LOOKAHEAD_BUFFER_SIZE, 0L);
  }

  @Override
  protected boolean fill() throws IOException {
    final SeekableInputStream sis = getSource();
    synchronized (sis) {
      final long fp = sis.getStreamPosition();
      if (fp != streamPosition) {
        sis.seek(streamPosition);
      }
      final boolean result = super.fill();
      streamPosition = sis.getStreamPosition();
      return result;
    }
  }

  @Override
  public void seek(long offset) throws IOException {
    if (getStreamPosition() != offset) {
      streamPosition = offset;
      resetBuffer();
    }
  }

  @Override
  public long getStreamPosition() throws IOException {
    return streamPosition - Math.max(0, getBufSize() - getInBufPos());
  }
}

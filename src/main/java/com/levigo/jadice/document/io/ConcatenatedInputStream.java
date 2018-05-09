package com.levigo.jadice.document.io;

import java.io.IOException;
import java.util.ArrayList;

/**
 * ConcatenatedInputStream is a small class giving the ability to concatenate
 * <code>InputStream</code>s to a new big one.
 * <p>
 * to append a InputStream use the method {@link #appendInputStream(SeekableInputStream)}.
 * <p>
 * if no <code>InputStream</code> is appended any read method will throw a IOException
 */
public class ConcatenatedInputStream extends SeekableInputStream {

  /**
   * When reading into byte buffers, try to read at least this number of bytes, if the room in the
   * buffer allows us to do so.
   * <p>
   * See https://support.levigo.de/jira/browse/DOCP-447 for reasons for this feature.
   */
  private static final int PREFERRED_MINIMUM_READ_CHUNK = 16;
  private final ArrayList<SeekableInputStream> inputStreams;
  private final ArrayList<Long> streamLengths;
  private SeekableInputStream currentInputStream;
  private int currentInputStreamNum = -1;
  private boolean reachedEOF = false;
  private long currentStreamPos = 0;
  private long absoluteStreamPosition = 0;
  private int estimatedSize = -1;

  /**
   * create an <code>ConcatenatedInputStream</code>
   */
  public ConcatenatedInputStream() {
    inputStreams = new ArrayList<>(4);
    streamLengths = new ArrayList<>(4);
  }

  @Override
  public int read() throws IOException {
    checkClosed();
    if (reachedEOF)
      return -1;
    int c = currentInputStream.read();
    if (c < 0) {
      // save the currentStreamPos as length
      if (streamLengths.size() < currentInputStreamNum)
        streamLengths.add(currentInputStreamNum - 1, Long.valueOf(currentStreamPos));
      currentStreamPos = 0;

      // we've reached the end of this input stream
      // switch to the next if possible
      if (!inputStreams.isEmpty()) {
        // get the next input stream
        if ((currentInputStreamNum + 1) < inputStreams.size()) {
          currentInputStream = inputStreams.get(++currentInputStreamNum);
          currentInputStream.seek(0);
          c = this.read();
        } else {
          reachedEOF = true;
          return -1;
        }
      } else {
        // we've reached the EOF
        reachedEOF = true;
        currentInputStream = null;
        return -1;
      }
    } else {
      absoluteStreamPosition++;
      currentStreamPos++;
    }
    bitOffset = 0;
    return c;
  }

  @Override
  public int read(byte[] b, final int off, final int len) throws IOException {
    checkClosed();
    if (currentInputStream == null && inputStreams.isEmpty())
      return -1;
    if (reachedEOF)
      return -1;

    int read = 0;
    while (read < Math.min(PREFERRED_MINIMUM_READ_CHUNK, len - read)) {
      final int r = doRead(b, off + read, len - read);
      if (r < 0) {
        if (read == 0)
          return -1;
        else
          break;
      }

      read += r;
    }

    bitOffset = 0;
    return read;
  }

  /**
   * @param b
   * @param off
   * @param len
   * @return
   * @throws IOException
   */
  private int doRead(byte[] b, int off, int len) throws IOException {
    int read = currentInputStream.read(b, off, len);

    if (read < 0) {
      if (streamLengths.size() < currentInputStreamNum)
        streamLengths.add(currentInputStreamNum - 1, Long.valueOf(currentStreamPos));

      currentStreamPos = 0;
      // we've reached the end of this input stream
      // switch to the next if possible
      if (!inputStreams.isEmpty()) {
        // get the next input stream
        if ((currentInputStreamNum + 1) < inputStreams.size()) {
          currentInputStream = inputStreams.get(++currentInputStreamNum);
          currentInputStream.seek(0);
          read = this.read(b, off, len);
        } else {
          reachedEOF = true;
          return -1;
        }
      } else {
        // we've reached the EOF
        reachedEOF = true;
        currentInputStream = null;
        return -1;
      }
    } else {
      absoluteStreamPosition += read;
      currentStreamPos += read;
    }

    return read;
  }

  /**
   * append the given <code>InputStream</code> to this <code>ConcatenatedInputStream</code>
   *
   * @param stream the <code>InputStream</code> to be appended
   * @throws IllegalArgumentException if the paramter <code>stream</code> is <code>null</code>
   */
  public void appendInputStream(SeekableInputStream stream) {
    if (stream == null)
      throw new IllegalArgumentException("passed stream has to not null");
    if (inputStreams.isEmpty()) {
      currentInputStream = stream;
      currentInputStreamNum++;
    }

    inputStreams.add(stream);
  }

  @Override
  public long getStreamPosition() throws IOException {
    checkClosed();
    return absoluteStreamPosition;
  }

  @Override
  public long length() throws IOException {
    checkClosed();
    if (streamLengths.size() == inputStreams.size()) {
      // FIXME:CK: length calculation works not correctly
      long calculatedLength = 0;
      for (final Long len : streamLengths) {
        calculatedLength += len.longValue();
      }
      return calculatedLength;
    } else {
      long calculatedLength = 0;
      for (final SeekableInputStream stream : inputStreams) {
        final long len = stream.length();
        if (len >= 0)
          calculatedLength += len;
        else
          return -1L;
      }

      return calculatedLength;
    }
  }

  @Override
  public void seek(long pos) throws IOException {
    // System.err.println("seek to: "+pos);
    checkClosed();
    if (pos < flushedPos) {
      throw new IndexOutOfBoundsException("pos < flushedPos!");
    }
    bitOffset = 0;

    if (absoluteStreamPosition == pos)
      return;
    if (absoluteStreamPosition > pos) {
      reachedEOF = false;

      // FIXME: CK: noch optimieren. Mit this.streamLengths klappt es
      // nicht da ist noch ein Fehler drinne.
      currentInputStreamNum = 0;
      currentStreamPos = 0;
      currentInputStream = inputStreams.get(0);
      currentInputStream.seek(0);
      absoluteStreamPosition = 0;
    }

    final byte[] tmp = new byte[1024];
    while (!reachedEOF && absoluteStreamPosition < pos)
      if (this.read(tmp, 0, Math.min(tmp.length, (int) (pos - absoluteStreamPosition))) < 0)
        break;

    absoluteStreamPosition = pos;
  }

  @Override
  public long getSizeEstimate() {
    if (estimatedSize < 0) {
      final ArrayList<SeekableInputStream> streams = new ArrayList<>(inputStreams);
      for (final SeekableInputStream seekableInputStream : streams) {
        estimatedSize += seekableInputStream.getSizeEstimate();
      }
    }
    return estimatedSize;
  }
}

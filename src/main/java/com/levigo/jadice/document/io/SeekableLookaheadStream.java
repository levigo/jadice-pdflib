package com.levigo.jadice.document.io;

import java.io.IOException;
import java.nio.ByteBuffer;


public class SeekableLookaheadStream extends SeekableInputStream {

  protected static final int DEFAULT_LOOKAHEAD_BUFFER_SIZE = 4096;

  public static final class MaxLookaheadReachedException extends IOException {
    private static final long serialVersionUID = 3334656278766003144L;

    public MaxLookaheadReachedException(String s) {
      super(s);
    }
  }

  private final SeekableInputStream source;
  private final ByteBuffer buf;
  /**
   * keeping inBufPos to determine position in the buffer to enable lookahead support
   */
  private int inBufPos = 0;
  /**
   * using bufSize instead of buf.capacity() as the buffer may be filled with less bytes than
   * possible
   */
  private int bufSize = 0;

  public SeekableLookaheadStream(SeekableInputStream source, int maxLookahead) {
    if (source == null)
      throw new IllegalArgumentException("source stream must not be null");

    this.source = source;

    buf = ByteBuffer.allocate(maxLookahead);
  }

  public SeekableLookaheadStream(SeekableInputStream source) {
    this(source, DEFAULT_LOOKAHEAD_BUFFER_SIZE);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    bitOffset = 0;

    if (inBufPos < bufSize) {

      /*
       * we've got some data to fill into the requested array
       */
      int lenToRead = bufSize - inBufPos;
      lenToRead = Math.min(len, lenToRead);

      buf.position(inBufPos);

      /*
       * get the data out of the buffer
       */
      buf.get(b, off, lenToRead);

      inBufPos += lenToRead;

      return lenToRead;
    } else {

      /*
       * try to fill up the buffer
       */
      if (fill()) {

        /*
         * buffer has been filled up. recall read
         */
        return read(b, off, len);
      } else {

        /*
         * no more data available
         */
        return -1;
      }
    }
  }

  @Override
  public int read() throws IOException {
    bitOffset = 0;

    if (inBufPos < bufSize) {

      /*
       * there is enough data in the buffer
       */
      return buf.get(inBufPos++) & 0xff;

    } else {

      /*
       * not enough data in the buffer. Try to fill it
       */

      if (fill()) {

        /*
         * buffer has been filled up. Delegate read
         */

        return read();
      } else {

        /*
         * no more data in the stream
         */
        return -1;
      }
    }

  }

  @Override
  public long length() throws IOException {
    return source.length();
  }

  public int lookahead(int index) throws IOException {

    /*
     * working with index-1 as lookahead with index 0 doesn't make sense.
     */

    if (inBufPos + index - 1 < bufSize) {
      /*
       * there is enugh data in the buffer
       */
      return buf.get(inBufPos + index - 1) & 0xff;

    } else {

      /*
       * no enough data in the buffer. Try to fill it
       */

      if (fill()) {

        /*
         * buffer has been filled up. Delegate lookahead
         */

        return lookahead(index);
      } else {

        /*
         * no more data in the stream
         */
        return -1;
      }
    }
  }

  @Override
  public void seek(long offset) throws IOException {
    source.seek(offset);
    resetBuffer();
  }

  protected void resetBuffer() {
    inBufPos = 0;
    bufSize = 0;
    bitOffset = 0;
  }

  public SeekableInputStream getSource() {
    return source;
  }

  /**
   * consume the specified number of characters looked ahead.
   *
   * @param count count of lookahead characters to consume
   */
  public void consume(int count) throws IOException {

    if (inBufPos + count < bufSize) {
      /*
       * only increment the in buf pos
       */
      inBufPos += count;
    } else {


      /*
       * increment the position as far as possible
       */
      count -= bufSize - inBufPos;
      inBufPos = bufSize;

      /*
       * fill the buffer and then increment the position
       */
      if (fill()) {
        /*
         * calling consume recursively to ensure that all checks are done again after the buffer has
         * been filled up
         */
        consume(count);
      }
    }

  }

  @Override
  public long getStreamPosition() throws IOException {
    return source.getStreamPosition() - Math.max(0, (bufSize - inBufPos));
  }

  protected int getInBufPos() {
    return inBufPos;
  }

  protected int getBufSize() {
    return bufSize;
  }

  protected boolean fill() throws IOException {

    /*
     * determine the amount of bytes to be read.
     */
    final int len = buf.capacity() - Math.max(0, (bufSize - inBufPos));

    final byte[] b = new byte[len];


    int pos = 0;
    int read = 0;

    /*
     * trying to get as much data as possible to fill the buffer up
     */
    while ((b.length - pos > 0) && (read = source.read(b, pos, b.length - pos)) >= 0) {
      pos += read;
    }

    /*
     * if the position hasn't been changed no data is available on the stream
     */
    if (pos == 0) {
      return false;
    }


    /*
     * check if we've got old data we should use
     */
    if (bufSize - inBufPos > 0) {
      /*
       * appending exactly the size which has been read
       */
      buf.position(inBufPos);

      final byte[] oldData = new byte[bufSize - inBufPos];

      buf.get(oldData);

      /*
       * copy old Data to the beginning of the new data
       */
      buf.position(0);
      buf.put(oldData);

      /*
       * position the buffer at the end of the old data
       */
      buf.position(oldData.length);

      bufSize = oldData.length;

    } else {

      /*
       * no old data we should take care of
       */
      buf.position(0);

      bufSize = 0;
    }

    /*
     * copy the data read to the byteArray. (pos is the number of bytes read from the source stream)
     */
    buf.put(b, 0, pos);

    /*
     * reset old buffer
     */
    buf.position(0);

    /*
     * set new buffer position and size
     */
    inBufPos = 0;
    bufSize += pos;


    /*
     * the buffer has been filled up, return true
     */
    return true;
  }

  @Override
  public long getSizeEstimate() {
    return source.getSizeEstimate();
  }
}

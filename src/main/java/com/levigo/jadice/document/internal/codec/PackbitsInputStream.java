package com.levigo.jadice.document.internal.codec;

import java.io.IOException;

import com.levigo.jadice.document.io.ByteArrayBuffer;
import com.levigo.jadice.document.io.SeekableInputStream;

/**
 * This class implements a stream filter for uncompressing packbits data. It is also used as a part
 * of an uncompressing chain for uncompress image data. Instances of this class wrap an inputstream
 * whose data is compressed in packbits format and will be uncompressed on the fly while reading.
 */
public final class PackbitsInputStream extends SeekableInputStream {
  /**
   * A table for reversing the order of the bits in one byte. Initialised on-demand by
   * getReverseBitsTable().
   */
  private static byte[] reverseBitsTable = null;

  static {
    reverseBitsTable = new byte[256];
    for (int i = 0; i < reverseBitsTable.length; i++) {
      byte b = (byte) i;
      byte r = 0;
      for (int j = 0; j < 8; j++) {
        r |= b & 1;
        if (j != 7) {
          r <<= 1;
          b >>= 1;
        }
      }
      reverseBitsTable[i] = r;
    }
  }

  // uncompressed data
  private final ByteArrayBuffer uncompressedData;
  // data properties
  private final int scanlineStride;
  private final boolean fillOrder;
  private final byte[] tmpSip = new byte[4096];
  private long currentPosition = 0;
  // stream properties
  private boolean reachEOF = false;
  private boolean propagateClose = false;
  private long maxDataLength = -1;
  private SeekableInputStream stream;

  // FIXME:markReadLimit ignored
  // private int markReadLimit = 0;
  private long srcPosition = 0;
  private int sipped = 0;
  private int filled = -1;

  /**
   * Construct a PackbitsInputStream. It decodes "on-the-fly" Packbitscompressed data provided by
   * the given inputstream when invoking {@link #read()}, {@link #read(byte[])} or
   * {@link #read(byte[], int, int)}.
   *
   * @param in             the inputstream providing the Packbits compressed data to decode
   * @param scanlineStride indicates the number of bytes of data there is for a single line of image
   *                       data
   * @param fillOrder      indicates whether the data is provided in Motorola style byte ordering
   */
  public PackbitsInputStream(SeekableInputStream in, int scanlineStride, boolean fillOrder) {
    this(in, Long.MAX_VALUE, false, scanlineStride, fillOrder);
  }

  /**
   * Construct a PackbitsInputStream. It decodes "on-the-fly" Packbits compressed data provided by
   * the given inputstream when invoking {@link #read()}, {@link #read(byte[])} or
   * {@link #read(byte[], int, int)}.
   *
   * @param in             the inputstream providing the Packbits compressed data to decode
   * @param maxLength      the maximal length of uncompressed data. Per default all data will be
   *                       uncompressed until EOF is reached. The parameter maxlength allows to limit the
   *                       decompression.
   * @param scanlineStride indicates the number of bytes of data there is for a single line of image
   *                       data
   * @param fillOrder      indicates whether the data is provided in Motorola style byte ordering
   */
  public PackbitsInputStream(SeekableInputStream in, long maxLength, int scanlineStride, boolean fillOrder) {
    this(in, maxLength, false, scanlineStride, fillOrder);
  }

  /**
   * Construct a PackbitsInputStream. It decodes "on-the-fly" Packbits compressed data provided by
   * the given inputstream when invoking {@link #read()}, {@link #read(byte[])} or
   * {@link #read(byte[], int, int)}.
   *
   * @param in             the inputstream providing the Packbits compressed data to decode
   * @param maxLength      the maximal length of uncompressed data. Per default all data will be
   *                       uncompressed until EOF is reached. The parameter maxlength allows to limit the
   *                       decompression.
   * @param propagateClose indicates whether the embedded Packbits inputstream should be closed as
   *                       well if this stream will be closed.
   * @param scanlineStride indicates the number of bytes of data there is for a single line of image
   *                       data
   * @param fillOrder      indicates whether the data is provided in Motorola style byte ordering
   */
  public PackbitsInputStream(SeekableInputStream in, long maxLength, boolean propagateClose, int scanlineStride,
      boolean fillOrder) {

    if (in == null)
      throw new IllegalArgumentException("no inputstream");

    stream = in;
    maxDataLength = maxLength;
    this.propagateClose = propagateClose;
    uncompressedData = new ByteArrayBuffer();

    this.scanlineStride = scanlineStride;
    this.fillOrder = fillOrder;
  }

  /**
   * Reads a byte of uncompressed data. This method will block until enough input is available for
   * decompression.
   *
   * @return the byte read, or -1 if end of compressed input is reached
   * @throws IOException if an I/O error has occurred
   */
  @Override
  public int read() throws IOException {
    checkClosed();
    bitOffset = 0;
    if (currentPosition < uncompressedData.size() || fillUp())
      return (0xFF & uncompressedData.getByteAt((int) currentPosition++));
    return -1;
  }

  /**
   * Reads uncompressed data into an array of bytes. This method will block until some input can be
   * uncompressed.
   *
   * @param b the buffer into which the data is read
   * @return the actual number of bytes read, or -1 if the end of the compressed input is reached
   * @throws IOException if an I/O error has occurred
   */
  @Override
  public int read(byte[] b) throws IOException {
    if (b == null || b.length == 0)
      return 0;
    return read(b, 0, b.length);
  }

  /**
   * Reads uncompressed data into an array of bytes. This method will block until some input can be
   * uncompressed.
   *
   * @param b   the buffer into which the data is read
   * @param off the start offset of the data
   * @param len the maximum number of bytes read
   * @return the actual number of bytes read, or -1 if the end of the compressed input is reached
   * @throws IOException if an I/O error has occurred
   */
  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    checkClosed();
    bitOffset = 0;
    if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return 0;
    }
    if (currentPosition < uncompressedData.size() || fillUp()) {
      final int counter = uncompressedData.getBytesAt((int) currentPosition, b, off, len);
      currentPosition += counter;
      return counter;
    }

    return -1;
  }

  /**
   * Returns 0 after EOF has been reached, otherwise always return 1.
   * <p>
   * Programs should not count on this method to return the actual number of bytes that could be
   * read without blocking.
   *
   * @return 1 before EOF and 0 after EOF.
   * @throws IOException if an I/O error occurs.
   */
  @Override
  public int available() throws IOException {
    checkClosed();
    return reachEOF ? 0 : 1;
  }

  /**
   * Closes this input stream and releases any system resources associated with the stream.
   *
   * @throws IOException if an I/O error has occurred
   */
  @Override
  public void close() throws IOException {
    super.close();
    if (propagateClose && stream != null)
      stream.close();
    stream = null;
    if (uncompressedData != null)
      uncompressedData.clear();
  }

  /**
   * Fills up the uncompressed data buffer
   *
   * @return boolean indicator whether decompression took places and uncompressed data is available
   * @throws IOException
   */
  private boolean fillUp() throws IOException {
    int counter = 0;
    while (counter < scanlineStride) {
      final byte cmd = getNextCommand();
      if (reachEOF)
        break;

      if (cmd >= 0 && cmd <= 127) {
        final int copyCount = 1 + cmd;
        int size = uncompressedData.size();
        for (int i = 0; i < copyCount && size < maxDataLength; i++, size++) {
          final int data = readNext();
          if (!reachEOF) {
            uncompressedData.append((byte) data);
            counter++;
          } else
            return counter > 0;
        }
      } else if (cmd != -128) {
        final int repeatCount = 1 - cmd;

        final int data = readNext();
        if (!reachEOF) {
          int size = uncompressedData.size();
          for (int i = 0; i < repeatCount && size < maxDataLength; i++, size++) {
            uncompressedData.append((byte) data);
            counter++;
          }
        }
      }
    }
    if (uncompressedData.size() == maxDataLength && stream != null)
      if (!propagateClose) {
        stream = null;
      }

    return counter > 0;
  }

  /**
   * Reads the next byte of data from the input stream. The value byte is returned as an
   * <code>int</code> in the range <code>0</code> to <code>255</code>. If no byte is available
   * because the end of the stream has been reached, the value <code>-1</code> is returned. This
   * method blocks until input data is available, the end of the stream is detected, or an exception
   * is thrown.
   *
   * @return the next byte of data, or <code>-1</code> if the end of the stream is reached.
   * @throws IOException if an I/O error occurs.
   */
  private int readNext() throws IOException {
    checkClosed();
    if (stream == null)
      reachEOF = true;

    if (reachEOF)
      return -1;

    if ((filled == -1 || sipped == filled) && stream != null) {
      synchronized (stream) {
        stream.seek(srcPosition);
        filled = stream.read(tmpSip);
        srcPosition = stream.getStreamPosition();
        sipped = 0;
      }
    }
    final int next = (sipped < filled) ? 0xff & tmpSip[sipped++] : -1;
    if (-1 == next) {
      reachEOF = true;
      // CK: free stream as soon as possible, already finished data resides in
      // uncompressed data buffer afterwards
      if (!propagateClose) {
        stream = null;
      }
    }

    return next;
  }

  /**
   * Retrieves the next command code from the raw data
   *
   * @return the next command or <code>-1</code> if the end of the stream is reached.
   * @throws IOException if an I/O error occurs.
   */
  private byte getNextCommand() throws IOException {
    final int data = readNext();
    if (!reachEOF) {
      return fillOrder ? reverseBitsTable[data] : (byte) data;
    }
    return -1;
  }

  /**
   * @see com.levigo.jadice.document.io.SeekableInputStream#getStreamPosition()
   */
  @Override
  public long getStreamPosition() throws IOException {
    checkClosed();
    return currentPosition;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#length()
   */
  @Override
  public long length() throws IOException {
    return -1L;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#seek(long)
   */
  @Override
  public void seek(long pos) throws IOException {
    checkClosed();
    if (pos < getFlushedPosition() || pos < 0)
      throw new IndexOutOfBoundsException(
          "Position to seek to (" + pos + ") is smaller than the flushed position or negative.");

    bitOffset = 0;
    if (pos <= currentPosition || pos <= uncompressedData.size())
      currentPosition = pos;
    else {
      while (currentPosition < pos && !reachEOF) {
        if (!fillUp())
          break;
      }
      currentPosition = pos;
    }
  }

  @Override
  public long getSizeEstimate() {
    return maxDataLength != Long.MAX_VALUE ?
        (int) maxDataLength :
        (uncompressedData != null ? uncompressedData.size() : 1024);
  }

}

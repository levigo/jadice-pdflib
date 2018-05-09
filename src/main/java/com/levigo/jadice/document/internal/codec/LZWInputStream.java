package com.levigo.jadice.document.internal.codec;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.levigo.jadice.document.io.ByteArrayBuffer;
import com.levigo.jadice.document.io.SeekableInputStream;

/**
 * This class implements a stream filter for uncompressing lzw data. It is also used as a part of an
 * uncompressing chain for uncompress image data. Instances of this class wrap an inputstream whose
 * data is compressed in lzw format and will be uncompressed on the fly while reading. Prediction
 * schemes will not applied by this class.
 */
public final class LZWInputStream extends SeekableInputStream {
  private static final Logger LOGGER = LoggerFactory.getLogger(LZWInputStream.class);
  /**
   * The number of bytes to read-ahead. Default: {@value #READ_AHEAD_LENGTH}.
   */
  private static final int READ_AHEAD_LENGTH = 64;
  // uncompressing constants
  private static int CLEAR_CODE = 256; // = 1 << number of roots
  private final static byte[][] alphabetSoup = new byte[CLEAR_CODE][1];
  private static int EOI_CODE = 257; // = 1 << number of roots + 1
  private static int FIRST_FREE_CODE = 258; // = CLEAR_CODE + 2

  static {
    for (int i = 0; i < CLEAR_CODE; i++) {
      alphabetSoup[i] = new byte[]{(byte) i
      };
    }
  }

  private final boolean propagateClose;
  private final long maxDataLength;
  // uncompressing variables
  private byte dictionary[][];
  private int dictionaryLength;
  /**
   * The number of bits per code word
   */
  private int codeLength = 9;
  /**
   * The mask used to mask-out the code-word
   */
  private int codeMask = (1 << 9) - 1;
  /**
   * The maximum code to use before increasing the code length
   */
  private int codeMax = codeMask - 1; // default to early change
  private int nextDataFragment = 0;
  private int nextBits = 0;
  private int lastCode = 0;
  // uncompressed data
  private ByteArrayBuffer uncompressedData;
  private long currentPosition = 0;
  // stream properties
  private boolean reachedEOF = false;
  private SeekableInputStream stream;
  private long sourcePosition;

  private byte[] readAheadBuffer;
  private int readAheadConsumed = 0;
  private int readAheadFilled = -1;

  private boolean enableTIFF5CompatibilityCheck;
  private boolean useTIFF5Compatibility;

  private boolean useEarlyChange = true;

  /**
   * Construct a LZWInputStream. It decodes "on-the-fly" LZW compressed data provided by the given
   * inputstream when invoking {@link #read()}, {@link #read(byte[])} or
   * {@link #read(byte[], int, int)}.
   *
   * @param in the inputstream providing the LZW compressed data to decode
   */
  public LZWInputStream(SeekableInputStream in) {
    this(in, Long.MAX_VALUE, false);
  }

  /**
   * Construct a LZWInputStream. It decodes "on-the-fly" LZW compressed data provided by the given
   * inputstream when invoking {@link #read()}, {@link #read(byte[])} or
   * {@link #read(byte[], int, int)}.
   *
   * @param in        the inputstream providing the LZW compressed data to decode
   * @param maxLength the maximal length of uncompressed data. Per default all data will be
   *                  uncompressed until EOF is reached. The parameter maxlength allows to limit the
   *                  decompression.
   */
  public LZWInputStream(SeekableInputStream in, long maxLength) {
    this(in, maxLength, false);
  }

  /**
   * Construct a LZWInputStream. It decodes "on-the-fly" LZW compressed data provided by the given
   * inputstream when invoking {@link #read()}, {@link #read(byte[])} or
   * {@link #read(byte[], int, int)}.
   *
   * @param in             the inputstream providing the LZW compressed data to decode
   * @param maxLength      the maximal length of uncompressed data. Per default all data will be
   *                       uncompressed until EOF is reached. The parameter maxlength allows to limit the
   *                       decompression.
   * @param propagateClose indicates whether the embedded lzw inputstream should be closed as well
   *                       if this stream will be closed.
   */
  public LZWInputStream(SeekableInputStream in, long maxLength, boolean propagateClose) {
    if (in == null)
      throw new IllegalArgumentException("no inputstream");

    stream = in;
    maxDataLength = maxLength;
    this.propagateClose = propagateClose;
    uncompressedData = new ByteArrayBuffer();
    initializeDictionary();
    nextDataFragment = 0;
    nextBits = 0;
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

    if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return 0;
    }
    bitOffset = 0;
    if (currentPosition < uncompressedData.size() || fillUp()) {
      final int counter = uncompressedData.getBytesAt((int) currentPosition, b, off, len);
      currentPosition += counter;
      return counter;
    }
    return -1;
  }

  /**
   * Closes this input stream and releases any system resources associated with the stream.
   *
   * @throws IOException if an I/O error has occurred
   */
  @Override
  public void close() throws IOException {
    super.close();
    if (propagateClose && stream != null) {
      stream.close();
    }
    stream = null;
    if (uncompressedData != null)
      uncompressedData.clear();
    uncompressedData = null;
    dictionary = null;
  }

  /**
   * Fills up the uncompressed data buffer
   *
   * @return boolean indicator whether decompression took places and uncompressed data is available
   * @throws IOException
   */
  private boolean fillUp() throws IOException {
    // at the very start of the stream check for TIFF 5.0 encoded data
    if (enableTIFF5CompatibilityCheck && readAheadFilled == -1 && readAheadConsumed == 0) {
      if (readNext() == 0x00 && readNext() == 0x01) {
        useTIFF5Compatibility = true;
        readAheadConsumed = 0;
        setUseEarlyChange(false);
      } else
        readAheadConsumed = 0;
    }

    int code = 0;
    byte string[];
    final int size = uncompressedData.size();
    if (((code = getNextCode()) == EOI_CODE) || size >= maxDataLength) {
      clearResources();
      return false;
    }

    int length;
    if (code == CLEAR_CODE) {
      initializeDictionary();
      code = getNextCode();

      if (code == EOI_CODE || size >= maxDataLength) {
        clearResources();
        return false;
      }

      /*
       * length calculation has to use the Math.min(long, long) method as maxDataLength may exceed
       * Integer.MAX_VALUE. The result of such a computation is a negative value when casting
       * maxDataLength to int. That means the length will be negative which the underlying
       * ByteArrayBuffer will simply ignore (no exception will be thrown). This fillUp() method will
       * return true as it thinks that something has been filled in and returns true. The following
       * access to the ucompressed data will then fail
       */
      length = (int) Math.min(dictionary[code].length, (maxDataLength - size));
      uncompressedData.append(dictionary[code], 0, length);

      lastCode = code;
    } else {
      if (code < dictionaryLength) {
        string = dictionary[code];

        length = (int) Math.min(string.length, (maxDataLength - size));
        uncompressedData.append(string, 0, length);

        addToDictionary(concatData(dictionary[lastCode], string[0]));
        lastCode = code;
      } else {
        string = dictionary[lastCode];
        string = concatData(string, string[0]);
        length = (int) Math.min(string.length, (maxDataLength - size));
        uncompressedData.append(string, 0, length);

        addToDictionary(string);
        lastCode = code;
      }
    }
    if (size + length == maxDataLength)
      clearResources();

    return true;
  }

  private void clearResources() {
    // CK: free stream as soon as possible, already finished data resides in
    // uncompressed data buffer afterwards
    if (!propagateClose)
      stream = null;
    dictionary = null;
    readAheadBuffer = null;
  }

  /**
   * Initialize dictionary
   */
  private void initializeDictionary() {
    if (dictionary == null) {
      dictionary = new byte[4096 + (useEarlyChange ? 0 : 1024)][];
      System.arraycopy(alphabetSoup, 0, dictionary, 0, CLEAR_CODE);
    }

    dictionaryLength = FIRST_FREE_CODE;
    codeLength = 9;
    codeMask = (1 << codeLength) - 1;
    codeMax = useEarlyChange ? codeMask - 1 : codeMask;
  }

  /**
   * Adds a string to the dictionary.
   *
   * @param string the string to add
   */
  private void addToDictionary(byte string[]) {
    dictionary[dictionaryLength++] = string;

    if (dictionaryLength > codeMax) {
      codeLength++;
      if (codeLength > 12) {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("The code length exceeded 12 bits. Truncating it to 12 bits.");
        codeLength = 12;
      }

      codeMask = (1 << codeLength) - 1;
      codeMax = useEarlyChange ? codeMask - 1 : codeMask;
    }
  }

  /**
   * Concats given byte <code>newString</code> to the end of <code>oldString</code> byte array.
   *
   * @param oldString byte array to be extended
   * @param newString byte to be appended
   * @return the concatenated byte string
   */
  private byte[] concatData(byte oldString[], byte newString) {
    final int length = oldString.length;
    final byte string[] = new byte[length + 1];
    System.arraycopy(oldString, 0, string, 0, length);
    string[length] = newString;

    return string;
  }

  /**
   * Reads the next byte of data from the input stream. The value byte is returned as an
   * <code>int</code> in the range <code>0</code> to <code>255</code>. If no byte is available
   * because the end of the stream has been reached, the value <code>-1</code> is returned. This
   * method blocks until input data is available, the end of the stream is detected, or an exception
   * is thrown.
   *
   * @return the next byte of data, or <code>-1</code> if the end of the stream is reached.
   * @throws IOException if an I/O error occurs or a not supported lzw compression is detected.
   */
  private int readNext() throws IOException {
    checkClosed();

    if (reachedEOF)
      return -1;

    // allocate buffer if necessary
    if (null == readAheadBuffer)
      readAheadBuffer = new byte[READ_AHEAD_LENGTH];

    if ((readAheadFilled == -1 || readAheadConsumed >= readAheadFilled) && stream != null) {
      synchronized (stream) {
        if (stream.getStreamPosition() != sourcePosition)
          stream.seek(sourcePosition);
        readAheadFilled = stream.read(readAheadBuffer);
        sourcePosition += readAheadFilled;
        readAheadConsumed = 0;
      }
    }
    if (readAheadConsumed >= readAheadFilled) {
      reachedEOF = true;
      clearResources();
      return 0; // zero-padding at EOF mandated by the spec
    }

    final int next = 0xff & readAheadBuffer[readAheadConsumed++];

    return next;
  }

  /**
   * Returns the next code with the length of 9, 10, 11 or 12 bits. The code bit length increases
   * each time the filling degree exceed the border mark of (1 << code bit length - 1).
   *
   * @return the next code
   * @throws IOException if an I/O error occurs
   */
  private int getNextCode() throws IOException {
    if (useTIFF5Compatibility)
      return getNextCodeCompat();

    int data = readNext();
    if (!reachedEOF) {
      nextDataFragment = (nextDataFragment << 8) | (data & 0xff);
      nextBits += 8;

      if (nextBits < codeLength) {
        data = readNext();
        if (!reachedEOF) {
          nextDataFragment = (nextDataFragment << 8) | (data & 0xff);
          nextBits += 8;
        } else
          return EOI_CODE;
      }

      nextBits -= codeLength;

      return (nextDataFragment >> nextBits) & codeMask;
    }
    return EOI_CODE;
  }

  /**
   * Returns the next code int TIFF 5.0 compatibility mode.
   *
   * @return the next code
   * @throws IOException if an I/O error occurs
   */
  private int getNextCodeCompat() throws IOException {
    int data = readNext();
    if (!reachedEOF) {
      nextDataFragment |= (data & 0xff) << nextBits;
      nextBits += 8;

      if (nextBits < codeLength) {
        data = readNext();
        if (!reachedEOF) {
          nextDataFragment |= (data & 0xff) << nextBits;
          nextBits += 8;
        } else
          return EOI_CODE;
      }

      final int code = nextDataFragment & codeMask;

      nextDataFragment >>= codeLength;
      nextBits -= codeLength;

      return code;
    }
    return EOI_CODE;
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
      while (currentPosition < pos && !reachedEOF) {
        if (!fillUp())
          break;
      }
      currentPosition = pos;
    }
  }

  @Override
  public long getSizeEstimate() {
    return (maxDataLength != Long.MAX_VALUE //
        ? (int) maxDataLength : (uncompressedData != null ? uncompressedData.size() : 1024)) //
        + READ_AHEAD_LENGTH; // just to be on the safe side
  }

  public SeekableInputStream getSource() {
    return stream;
  }

  public boolean isUseEarlyChange() {
    return useEarlyChange;
  }

  /**
   * Set whether to use an one-off early code length increase or not. This setting defaults to
   * <code>true</code>. If the value of this entry is <code>false</code>, code length increases
   * shall be postponed as long as possible. If the value is <code>true</code>, code length
   * increases shall occur one code early. This parameter is included because LZW sample code
   * distributed by some vendors increases the code length one code earlier than necessary.
   *
   * @param useEarlyChange
   */
  public void setUseEarlyChange(boolean useEarlyChange) {
    if (readAheadFilled >= -1 && readAheadConsumed > 0)
      throw new IllegalStateException("It is too late to set the earlyChange mode");

    if (this.useEarlyChange == useEarlyChange)
      return;

    this.useEarlyChange = useEarlyChange;
    codeMax = useEarlyChange ? codeMask - 1 : codeMask;

    // must also reset the dictionary
    dictionary = null;
    initializeDictionary();
  }

  public boolean isEnableTIFF5CompatibilityCheck() {
    return enableTIFF5CompatibilityCheck;
  }

  /**
   * Set whether to enable the check for TIFF 5.0 encoded LZW data. This check is required, because
   * the libTIFF for spec Version 5.0 implemented a bunch of quirks that have been rectified
   * lateron.
   * <p>
   * To quote from the relevant section of libTIFF's tif_lzw.c:
   *
   * <blockquote> NB: The 5.0 spec describes a different algorithm than Aldus implements.
   * Specifically, Aldus does code length transitions one code earlier than should be done (for real
   * LZW). Earlier versions of this library implemented the correct LZW algorithm, but emitted codes
   * in a bit order opposite to the TIFF spec. Thus, to maintain compatibility w/ Aldus we interpret
   * MSB-LSB ordered codes to be images written w/ old versions of this library, but otherwise
   * adhere to the Aldus "off by one" algorithm.
   * <p>
   * Future revisions to the TIFF spec are expected to "clarify this issue". <blockquote>
   *
   * @param enableTIFF5CompatibilityCheck
   */
  public void setEnableTIFF5CompatibilityCheck(boolean enableTIFF5CompatibilityCheck) {
    this.enableTIFF5CompatibilityCheck = enableTIFF5CompatibilityCheck;
  }
}

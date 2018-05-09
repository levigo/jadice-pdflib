package com.levigo.jadice.document.internal.codec.tiff;

import java.awt.Dimension;
import java.io.EOFException;
import java.io.IOException;

import com.levigo.jadice.document.io.ByteArrayBuffer;
import com.levigo.jadice.document.io.SeekableInputStream;

/**
 * This class implements a predictor stream filter. It is also used as a part of
 * an uncompressing chain for decompress image data. Prediction scheme will be
 * applied each time data is read.
 */
public final class TIFFPredictorInputStream extends SeekableInputStream {

  private final int bytesPerRow;
  private final int predictor;
  private final int samplesPerPixel;
  private final int rowsPerStrip;
  private final Dimension size;
  private final int stripIndex;
  long absolutePosition = 0;
  int currentLineNumber = -1;
  private ByteArrayBuffer buffer;
  private SeekableInputStream stream;

  /**
   * Constructor
   *
   * @param stream          the inputstream proving data to apply preditor schemes on
   * @param predictorScheme the predictor scheme to apply. See therefore
   * @param samplesPerPixel the number of samples per pixel
   * @param bytesPerRow     the number of bytes per row
   * @param size            the pixel size
   * @see TIFF#PREDICTOR_NO_PREDICTION_SCHEME ,
   * @see TIFF#PREDICTOR_HORIZONTAL_DIFFERENCING ,
   * @see TIFF#PREDICTOR_FLOATINGPOINT .
   * <p>
   * Hint: The floating point predictor is not supported at the moment, because
   * its specification is not yet available from Adobe.
   */
  public TIFFPredictorInputStream(SeekableInputStream stream, int predictorScheme, int samplesPerPixel, int bytesPerRow,
      Dimension size) {
    this(stream, predictorScheme, samplesPerPixel, bytesPerRow, size.height, 0, size);
  }

  /**
   * Constructor
   *
   * @param stream          the inputstream proving data to apply preditor schemes on
   * @param predictorScheme the predictor scheme to apply. See therefore
   * @param samplesPerPixel the number of samples per pixel
   * @param bytesPerRow     the number of bytes per row
   * @param rowsPerStrip    the number of rows per strip
   * @param stripIndex      index of the strip
   * @param size            the pixel size
   * @see TIFF#PREDICTOR_NO_PREDICTION_SCHEME ,
   * @see TIFF#PREDICTOR_HORIZONTAL_DIFFERENCING ,
   * @see TIFF#PREDICTOR_FLOATINGPOINT .
   * <p>
   * Hint: The floating point predictor is not supported at the moment, because
   * its specification is not yet available from Adobe.
   */
  public TIFFPredictorInputStream(SeekableInputStream stream, int predictorScheme, int samplesPerPixel, int bytesPerRow,
      int rowsPerStrip, int stripIndex, Dimension size) {

    if (stream == null)
      throw new IllegalArgumentException("No InputStream");

    this.stream = stream;
    switch (predictorScheme){
      case TIFF.PREDICTOR_NO_PREDICTION_SCHEME:
      case TIFF.PREDICTOR_HORIZONTAL_DIFFERENCING:
        predictor = predictorScheme;
        break;
      case TIFF.PREDICTOR_FLOATINGPOINT:
        throw new IllegalArgumentException(
            "Floating point horizontal differencing not supported: Floating point predictor specification is not yet available from Adobe");
      default:
        throw new IllegalArgumentException("No valid predictor scheme: " + predictorScheme);
    }

    this.absolutePosition = 0;
    this.samplesPerPixel = samplesPerPixel;
    this.size = size;
    this.rowsPerStrip = rowsPerStrip;
    this.stripIndex = stripIndex;
    this.bytesPerRow = bytesPerRow;
    this.buffer = new ByteArrayBuffer(bytesPerRow);
    currentLineNumber = stripIndex * rowsPerStrip - 1;
  }

  /**
   * @see java.io.InputStream#read()
   */
  public int read() throws IOException {
    checkClosed();
    bitOffset = 0;
    if (absolutePosition >= buffer.size())
      if (!fillUp())
        return -1;

    return buffer.getByteAt((int) absolutePosition++);
  }

  /**
   * @see java.io.InputStream#read(byte[])
   */
  public int read(byte[] b) throws IOException {
    if (b == null || b.length == 0)
      return 0;
    return read(b, 0, b.length);
  }

  /**
   * @see java.io.InputStream#read(byte[], int, int)
   */
  public int read(final byte[] b, final int off, final int len) throws IOException {

    if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
      throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > b.length!");
    }
    if (len == 0)
      return 0;

    checkClosed();
    bitOffset = 0;

    int read = 0;
    while (read < len) {
      if (absolutePosition >= buffer.size())
        if (!fillUp())
          return read == 0 ? -1 : read;

      int toCopy = Math.min(len - read, buffer.size() - (int) absolutePosition);
      toCopy = buffer.getBytesAt((int) absolutePosition, b, off + read, toCopy);
      absolutePosition += toCopy;
      read += toCopy;
    }

    return read;
  }

  /**
   * Fills up the uncompressed data buffer
   *
   * @return boolean indicator whether decompression takes places and
   * uncompressed data is available
   * @throws IOException
   */
  private synchronized boolean fillUp() throws IOException {
    if ((currentLineNumber + 1) >= Math.min(size.height, (stripIndex + 1) * rowsPerStrip))
      return false;

    byte[] currentLine = new byte[bytesPerRow];
    if (this.stream != null)
      try {
        this.stream.readFully(currentLine, 0, currentLine.length);
      } catch (EOFException e) {
        //CK: free stream as soon as possible, already finished data resides in buffer afterwards
        stream = null;
        return false;
      }
    else
      return false;

    switch (predictor){
      case TIFF.PREDICTOR_NO_PREDICTION_SCHEME:
        break;
      case TIFF.PREDICTOR_HORIZONTAL_DIFFERENCING:
        applyHorizontalDifferencing(currentLine);
        break;
      case TIFF.PREDICTOR_FLOATINGPOINT:
        applyFloatingPoint();
        break;
      default:
        // should not happen!
        throw new IllegalStateException("predictor scheme unknown.");
    }
    currentLineNumber++;
    buffer.append(currentLine);

    if ((currentLineNumber + 1) >= Math.min(size.height, (stripIndex + 1) * rowsPerStrip)) {
      //CK: free stream as soon as possible, already finished data resides in buffer afterwards
      stream = null;
    }

    return true;
  }

  /**
   * Applies horizontal predictor on the current line
   *
   * @param currentLine
   */
  private void applyHorizontalDifferencing(byte[] currentLine) {
    // CK: reduce mathematical predictor operand to a simple difference
    for (int i = samplesPerPixel; i < currentLine.length - samplesPerPixel; i++) {
      currentLine[i] += currentLine[i - samplesPerPixel];
    }
  }

  /**
   * Applies floating point predictor on the current line
   */
  private void applyFloatingPoint() {
    // FIXME: Floating point predictor specification is not yet available from
    // Adobe
  }

  /**
   * @see com.levigo.jadice.document.io.SeekableInputStream#getStreamPosition()
   */
  public long getStreamPosition() throws IOException {
    checkClosed();
    return absolutePosition;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#length()
   */
  public long length() throws IOException {
    checkClosed();
    return -1L;
  }

  /**
   * @see javax.imageio.stream.ImageInputStream#seek(long)
   */
  public void seek(long pos) throws IOException {
    checkClosed();

    if (pos < this.flushedPos) {
      throw new IndexOutOfBoundsException("pos < flushedPos!");
    }
    this.bitOffset = 0;
    if (pos < buffer.size()) {
      absolutePosition = pos;
      currentLineNumber = (stripIndex * rowsPerStrip - 1) + (int) (pos / bytesPerRow);
      return;
    } else {
      while (absolutePosition < pos) {
        if (!fillUp())
          break;
      }
      absolutePosition = pos;
    }
  }

  /**
   * @see java.io.Closeable#close()
   */
  public void close() throws IOException {
    super.close();
    stream = null;
    if (buffer != null)
      buffer.clear();
    buffer = null;
  }

  public long getSizeEstimate() {
    return (buffer != null ? buffer.size() : 1024) + (stream != null ? stream.getSizeEstimate() : 1024);
  }
}

package com.levigo.jadice.document.internal.codec;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.levigo.jadice.document.io.SeekableInputStream;

/**
 * This stream applies a PNG predictor filter method on the given source data stream.
 * <p>
 * There are five basic PNG predictor filter types:
 *
 * <pre>
 * Type    Name
 *    0       None
 *    1       Sub
 *    2       Up
 *    3       Average
 *    4       Paeth
 * </pre>
 * <p>
 * The actual applied filter is specified within the given source data.
 */
public final class PNGPredictorInputStream extends SeekableInputStream {

  private static final int PNG_FILTER_NONE = 0;
  private static final int PNG_FILTER_SUB = 1;
  private static final int PNG_FILTER_UP = 2;
  private static final int PNG_FILTER_AVERAGE = 3;
  private static final int PNG_FILTER_PAETH = 4;

  private static final Logger LOGGER = LoggerFactory.getLogger(PNGPredictorInputStream.class);

  /**
   * The Sub filter transmits the difference between each byte and the value of the corresponding byte
   * of the prior pixel.
   * <p>
   * To compute the Sub filter, apply the following formula to each byte of the scanline:
   *
   *
   * <pre>
   * Sub(x) = Raw(x) - Raw(x - tbpp)
   * </pre>
   * <p>
   * where <tt>x</tt> ranges from zero to the number of bytes representing the scanline minus one,
   * <tt>Raw(x)</tt> refers to the raw data byte at that byte position in the scanline, and
   * <tt>tbpp</tt> is defined as the number of bytes per complete pixel, rounding up to one.
   *
   * @param curr
   * @param count
   * @param tbpp
   */
  private static void decodeSubFilter(byte[] curr, int count, int tbpp) {
    for (int i = tbpp; i < count; i++) {
      int val;

      val = curr[i] & 0xff;
      val += curr[i - tbpp] & 0xff;

      curr[i] = (byte) val;
    }
  }

  /**
   * The Up filter is just like the Sub filter except that the pixel immediately above the current
   * pixel, rather than just to its left, is used as the predictor.
   * <p>
   * To compute the Up filter, apply the following formula to each byte of the scanline:
   *
   *
   * <pre>
   * Up(x) = Raw(x) - Prior(x)
   * </pre>
   * <p>
   * where <tt>x</tt> ranges from zero to the number of bytes representing the scanline minus one,
   * <tt>Raw(x)</tt> refers to the raw data byte at that byte position in the scanline, and
   * <tt>Prior(x)</tt> refers to the unfiltered bytes of the prior scanline.
   *
   * @param curr
   * @param prev
   * @param count
   */
  private static void decodeUpFilter(byte[] curr, byte[] prev, int count) {
    for (int i = 0; i < count; i++) {
      final int raw = curr[i] & 0xff;
      final int prior = prev[i] & 0xff;

      curr[i] = (byte) (raw + prior);
    }
  }

  /**
   * The Average filter uses the average of the two neighboring pixels (left and above) to predict the
   * value of a pixel.
   * <p>
   * <p>
   * To compute the Average filter, apply the following formula to each byte of the scanline:
   *
   *
   * <pre>
   * Average(x) = Raw(x) - floor((Raw(x - tbpp) + Prior(x)) / 2)
   * </pre>
   * <p>
   * where <tt>x</tt> ranges from zero to the number of bytes representing the scanline minus one,
   * <tt>Raw(x)</tt> refers to the raw data byte at that byte position in the scanline,
   * <tt>Prior(x)</tt> refers to the unfiltered bytes of the prior scanline, and <tt>tbpp</tt> is
   * defined as for the Sub filter.
   * <p>
   * The subtraction of the predicted value from the raw byte must be done modulo 256, so that both
   * the inputs and outputs fit into bytes. However, the sum <tt>Raw(x-tbpp)+Prior(x)</tt> must be
   * formed without overflow (using at least nine-bit arithmetic). <tt>floor()</tt> indicates that the
   * result of the division is rounded to the next lower integer if fractional; in other words, it is
   * an integer division or right shift operation.
   *
   * @param curr
   * @param prev
   * @param count
   * @param tbpp
   */
  private static void decodeAverageFilter(byte[] curr, byte[] prev, int count, int tbpp) {
    int raw, priorPixel, priorRow;

    for (int i = 0; i < tbpp; i++) {
      raw = curr[i] & 0xff;
      priorRow = prev[i] & 0xff;

      curr[i] = (byte) (raw + priorRow / 2);
    }

    for (int i = tbpp; i < count; i++) {
      raw = curr[i] & 0xff;
      priorPixel = curr[i - tbpp] & 0xff;
      priorRow = prev[i] & 0xff;

      curr[i] = (byte) (raw + (priorPixel + priorRow) / 2);
    }
  }

  private static int paethPredictor(int a, int b, int c) {
    final int p = a + b - c;
    final int pa = Math.abs(p - a);
    final int pb = Math.abs(p - b);
    final int pc = Math.abs(p - c);

    if (pa <= pb && pa <= pc) {
      return a;
    } else if (pb <= pc) {
      return b;
    } else {
      return c;
    }
  }

  /**
   * The Paeth filter computes a simple linear function of the three neighboring pixels (left, above,
   * upper left), then chooses as predictor the neighboring pixel closest to the computed value. This
   * technique is due to Alan W. Paeth
   * <p>
   * <p>
   * To compute the Paeth filter, apply the following formula to each byte of the scanline:
   *
   *
   * <pre>
   * Paeth(x) = Raw(x) - PaethPredictor(Raw(x - tbpp), Prior(x), Prior(x - tbpp))
   * </pre>
   * <p>
   * where <tt>x</tt> ranges from zero to the number of bytes representing the scanline minus one,
   * <tt>Raw(x)</tt> refers to the raw data byte at that byte position in the scanline,
   * <tt>Prior(x)</tt> refers to the unfiltered bytes of the prior scanline, and <tt>tbpp</tt> is
   * defined as for the Sub filter.
   * <p>
   * <p>
   * Note this is done for each <strong>byte</strong>, regardless of bit depth. Unsigned arithmetic
   * modulo 256 is used, so that both the inputs and outputs fit into bytes. The sequence of
   * <tt>Paeth</tt> values is transmitted as the filtered scanline.
   * <p>
   * <p>
   * The PaethPredictor function is defined by the following pseudocode:
   *
   *
   * <pre>
   * function PaethPredictor (a, b, c)
   *    begin
   *         ; a = left, b = above, c = upper left
   *         p := a + b - c        ; initial estimate
   *         pa := abs(p - a)      ; distances to a, b, c
   *         pb := abs(p - b)
   *         pc := abs(p - c)
   *         ; return nearest of a,b,c,
   *         ; breaking ties in order a,b,c.
   *         if pa &lt;= pb AND pa &lt;= pc then return a
   *         else if pb &lt;= pc then return b
   *         else return c
   *    end
   * </pre>
   * <p>
   * The calculations within the PaethPredictor function must be performed exactly, without overflow.
   * Arithmetic modulo 256 is to be used only for the final step of subtracting the function result
   * from the target byte value.
   *
   *
   * <strong>Note that the order in which ties are broken is critical and must not be
   * altered.</strong> The tie break order is: pixel to the left, pixel above, pixel to the upper
   * left. (This order differs from that given in Paeth's article.)
   *
   * @param curr
   * @param prev
   * @param count
   * @param tbpp
   */
  private static void decodePaethFilter(byte[] curr, byte[] prev, int count, int tbpp) {
    int raw, priorPixel, priorRow, priorRowPixel;

    for (int i = 0; i < tbpp; i++) {
      raw = curr[i] & 0xff;
      priorRow = prev[i] & 0xff;

      curr[i] = (byte) (raw + priorRow);
    }

    for (int i = tbpp; i < count; i++) {
      raw = curr[i] & 0xff;
      priorPixel = curr[i - tbpp] & 0xff;
      priorRow = prev[i] & 0xff;
      priorRowPixel = prev[i - tbpp] & 0xff;

      curr[i] = (byte) (raw + paethPredictor(priorPixel, priorRow, priorRowPixel));
    }
  }

  private final int bytesPerRow;
  private final boolean propagateClose;

  /**
   * the number of bytes per complete pixel, rounding up to one.
   */
  private final int totalNumberBytesPerPixels;
  private long streamPos = 0;
  private byte currentLine[];
  private byte referenceLine[];
  private int positionInCurrentLine = -1;
  private SeekableInputStream stream;

  public PNGPredictorInputStream(SeekableInputStream stream, int colors, int columns, int bpc) {
    this(stream, colors, columns, bpc, false);
  }

  public PNGPredictorInputStream(SeekableInputStream stream, int colors, int columns, int bpc, boolean propagateClose) {
    this.stream = stream;

    final int bpp = bpc * colors;
    totalNumberBytesPerPixels = (int) Math.ceil(bpp / 8.0);

    bytesPerRow = (columns * bpp + 7) / 8;

    currentLine = new byte[bytesPerRow];
    referenceLine = new byte[bytesPerRow];
    this.propagateClose = propagateClose;
  }

  @Override
  public int read() throws IOException {
    checkClosed();
    bitOffset = 0;
    if (positionInCurrentLine < 0 || positionInCurrentLine >= currentLine.length)
      if (!fill())
        return -1;

    streamPos++;

    return currentLine[positionInCurrentLine++] & 0xff;
  }

  @Override
  public int read(final byte[] b, final int off, final int len) throws IOException {
    checkClosed();
    bitOffset = 0;
    int read = 0;
    while (read < len) {
      if (positionInCurrentLine < 0 || positionInCurrentLine >= currentLine.length)
        if (!fill())
          return read == 0 ? -1 : read;

      final int toCopy = Math.min(len - read, currentLine.length - positionInCurrentLine);
      System.arraycopy(currentLine, positionInCurrentLine, b, off + read, toCopy);
      read += toCopy;
      positionInCurrentLine += toCopy;
      streamPos += toCopy;
    }

    return read;
  }

  private boolean fill() throws IOException {
    // swap current and reference line
    final byte tmp[] = referenceLine;
    referenceLine = currentLine;
    currentLine = tmp;

    final int filter = stream.read();
    if (filter < 0)
      return false;

    fillLine(currentLine, stream);

    switch (filter){
      case PNG_FILTER_NONE:
        break;
      case PNG_FILTER_SUB:
        decodeSubFilter(currentLine, bytesPerRow, totalNumberBytesPerPixels);
        break;
      case PNG_FILTER_UP:
        decodeUpFilter(currentLine, referenceLine, bytesPerRow);
        break;
      case PNG_FILTER_AVERAGE:
        decodeAverageFilter(currentLine, referenceLine, bytesPerRow, totalNumberBytesPerPixels);
        break;
      case PNG_FILTER_PAETH:
        decodePaethFilter(currentLine, referenceLine, bytesPerRow, totalNumberBytesPerPixels);
        break;
      default:
        // Error -- unknown filter type
        throw new RuntimeException("PNG filter type #" + filter + " unknown.");
    }

    positionInCurrentLine = 0;

    return true;
  }

  private void fillLine(byte[] lineToFill, SeekableInputStream sourceStream) throws IOException {
    int offset = 0;
    int bytesRemaining = lineToFill.length;
    while (bytesRemaining > 0) {
      final int bytesRead = sourceStream.read(lineToFill, offset, bytesRemaining);
      if (bytesRead == -1) {
        // DOCPV-22 There are broken PDF files which hold too little data for the last line of a
        // PNG-predicted stream. Don't throw exception, simply ignore.
        LOGGER.debug("Early EOF. Unable to fill line. Number of bytes remaining: " + bytesRemaining);
        // upon EOF, simply leave the rest of the array untouched. We don't know what's currently in the
        // array. We don't care and are going to apply the predictor function to it anyway. Note that other
        // applications (Acrobat XI, Apache PDFBox) seem to copy the last line's content to the rest of the
        // array, without applying the predictor function another time.
        break;
      }
      offset += bytesRead;
      bytesRemaining -= bytesRead;
    }
  }

  @Override
  public long getStreamPosition() throws IOException {
    checkClosed();
    return streamPos;
  }

  @Override
  public long length() throws IOException {
    checkClosed();
    return stream.length();
  }

  /**
   * {@inheritDoc}
   * <p>
   * Performance considerations: Since PNG prediction/filtering depends on data read in the previous
   * and/or current line, seeking to a different position may cause significant amounts of data to be
   * decoded. In particular, when seeking backwards beyond the current line, decoding must be
   * re-started from position 0.
   */
  @Override
  public void seek(long pos) throws IOException {
    checkClosed();

    if (pos < flushedPos) {
      throw new IndexOutOfBoundsException("pos < flushedPos!");
    }
    bitOffset = 0;

    if (streamPos != pos) {
      long toSeek = pos - streamPos;
      if (toSeek < 0) {
        // this is where it gets expensive because we need to start reading from the beginning ...
        positionInCurrentLine = -1;
        streamPos = 0;
        stream.seek(0);
        Arrays.fill(currentLine, (byte) 0); // DOCPV-2464: reset to initial state
        Arrays.fill(referenceLine, (byte) 0); // DOCPV-2464: reset to initial state

        toSeek = pos;
      }

      // seek within buffered line
      final long restOfLine = bytesPerRow - positionInCurrentLine;
      if (toSeek <= restOfLine && positionInCurrentLine >= 0) {
        positionInCurrentLine += toSeek;
        streamPos += toSeek;
      } else {
        final byte[] tmp = new byte[1024];
        while (streamPos < pos && //
            this.read(tmp, 0, Math.min(tmp.length, (int) (pos - streamPos))) > -1) {
          // do nothing, just read to stream pos
        }

        streamPos = pos;


        // fixme: ...und so könnte es noch schneller gehen...
        // aber da ist irgendwo ein mini denkfehler drinne, denn
        // ab und zu werden die bildle ein wenig rosa...

        // toSeek -= restOfLine;
        // this.streamPos += restOfLine;
        //
        // fill();
        //
        // // while (streamPos < pos && read() > -1);
        //
        // // seek in bytesPerRow chunks as this is faster than skipping single
        // // bytes
        // while (toSeek > bytesPerRow) {
        // fill();
        // toSeek -= bytesPerRow;
        // this.streamPos += bytesPerRow;
        // }
        //
        // // seek rest
        // this.streamPos += toSeek;
        // this.currentPosition += toSeek;
      }
    }
  }


  @Override
  public void close() throws IOException {
    super.close();
    if (propagateClose)
      stream.close();
    stream = null;
  }


  @Override
  public long getSizeEstimate() {
    return stream != null ? stream.getSizeEstimate() : 1024;
  }

}

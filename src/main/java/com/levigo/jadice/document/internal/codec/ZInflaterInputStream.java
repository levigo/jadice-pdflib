package com.levigo.jadice.document.internal.codec;

import static java.lang.Math.min;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZStream;
import com.levigo.jadice.document.io.SeekableInputStream;

/**
 * This class implements a stream filter for uncompressing flate data. It could also be used as a
 * part of an uncompressing chain for uncompress data. Instances of this class wraps an inputstream
 * whose data is flate compressed and will be uncompressed on the fly while reading.
 * <p>
 * Java Platform API provides packages 'java.util.zip.*' for accessing to zlib, but that support is
 * very limited if you need to use the essence of zlib and suboptimal for the asynchronous usage as
 * SeekableInputStream. So this class use an external library for decompression.
 * <p>
 * Decompression will be done by JZlib, which is a re-implementation of zlib in pure Java. JZlib is
 * licensed under BSD style license Copyright (c) 2000,2001, 2002,2003 ymnk, JCraft,Inc.
 * <p>
 * For using this class developers have to ensure JZlib package is available in the classpath.
 */
public final class ZInflaterInputStream extends SeekableInputStream {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZInflaterInputStream.class);
  /**
   * flag, whether wrapped stream should be closed when this stream is closed
   */
  private final boolean propagateClose;
  /**
   * internal buffer to read ahead
   */
  private final byte[] uncompressedBuffer;
  /**
   * flate in data
   */
  private SeekableInputStream stream;
  private long sourcePosition;
  /**
   * current stream position equals to number of uncompressed, read data
   */
  private long position;
  /**
   * decompressor
   */
  private ZStream zStream;
  /**
   * internal flag if input data is exhausted
   */
  private boolean reachEOF;
  /**
   * filling degree of internal buffer
   */
  private int uncompressedBufferFillDegree;
  /**
   * current read index of internal buffer
   */
  private int uncompressedBufferIndex;
  /**
   * buffer size of internal buffer
   */
  private int bufferSize = 1024;

  /**
   * in case of bad data, this field is set to the position at which the underlying {@link ZStream}
   * is no longer readable
   */
  private long badDataPosition = -1;

  /**
   * Construct a ZInflaterInputStream instance. It decodes flate data provided by the given
   * inputstream on the fly when invoking one of the following methods {@link #read()},
   * {@link #read(byte[])} or {@link #read(byte[], int, int)}.
   *
   * @param in the inputstream providing the flate data to decode
   */
  public ZInflaterInputStream(SeekableInputStream in) {
    this(in, false, 1024);
  }

  /**
   * Construct a ZInflaterInputStream instance. It decodes flate data provided by the given
   * inputstream on the fly when invoking one of the following methods {@link #read()},
   * {@link #read(byte[])} or {@link #read(byte[], int, int)}.
   *
   * @param in         the inputstream providing the flate data to decode
   * @param bufferSize buffer size of internal read ahead buffer
   */
  public ZInflaterInputStream(SeekableInputStream in, int bufferSize) {
    this(in, false, bufferSize);
  }

  /**
   * Construct a ZInflaterInputStream instance. It decodes flate data provided by the given
   * inputstream on the fly when invoking one of the following methods {@link #read()},
   * {@link #read(byte[])} or {@link #read(byte[], int, int)}.
   *
   * @param in             the inputstream providing the flate data to decode
   * @param propagateClose indicates whether the embedded inputstream should be closed as well if
   *                       this stream will be closed.
   */
  public ZInflaterInputStream(SeekableInputStream in, boolean propagateClose) {
    this(in, propagateClose, 1024);
  }

  /**
   * Construct a ZInflaterInputStream instance. It decodes flate data provided by the given
   * inputstream on the fly when invoking one of the following methods {@link #read()},
   * {@link #read(byte[])} or {@link #read(byte[], int, int)}.
   *
   * @param in             the inputstream providing the flate data to decode
   * @param propagateClose indicates whether the embedded inputstream should be closed as well if
   *                       this stream will be closed.
   * @param bufferSize     buffer size of internal read ahead buffer
   */
  public ZInflaterInputStream(SeekableInputStream in, boolean propagateClose, int bufferSize) {
    if (in == null) {
      throw new IllegalArgumentException("Raw data input is null.");
    }

    stream = in;
    this.bufferSize = Math.max(1, Math.abs(bufferSize));
    this.propagateClose = propagateClose;
    position = 0;
    reachEOF = false;
    prepareInflater();

    uncompressedBuffer = new byte[this.bufferSize];
    uncompressedBufferIndex = 0;
    uncompressedBufferFillDegree = 0;
  }

  private void prepareInflater() {
    // first stop and free running inflater instance
    if (zStream != null) {
      // !!! force end of inflating process
      zStream.inflateEnd();
      zStream.free();
      zStream = null;
    }

    // ...before reinitialise inflater instance
    zStream = new ZStream();
    zStream.inflateInit(false);
    if (zStream.next_in == null) {
      zStream.next_in = new byte[bufferSize];
    }
    zStream.next_in_index = 0;
    zStream.avail_in = 0;
  }

  @Override
  public int read() throws IOException {
    checkClosed();
    final byte[] singleByteBuf = new byte[1];
    if (read(singleByteBuf, 0, 1) == -1) {
      return -1;
    }
    return singleByteBuf[0] & 0xFF;
  }

  @Override
  public int read(byte[] b) throws IOException {
    if (b == null || b.length == 0) {
      return 0;
    }
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    checkClosed();
    bitOffset = 0;
    if ((off | len | off + len | b.length - (off + len)) < 0) {
      throw new IndexOutOfBoundsException();
    }
    if (len == 0) {
      return 0;
    }

    if (uncompressedBufferIndex < uncompressedBufferFillDegree) {
      final int toCopy = Math.min(len, uncompressedBufferFillDegree - uncompressedBufferIndex);
      System.arraycopy(uncompressedBuffer, uncompressedBufferIndex, b, off, toCopy);
      uncompressedBufferIndex += toCopy;
      position += toCopy;
      return toCopy;
    }

    if (badDataPosition != -1 && sourcePosition >= (badDataPosition - 1)) {
      // reached the bad data position. Just return end of file marker.
      return -1;
    }

    int err;
    int bufferLen;
    final boolean useInternalBuffer = true; // uncompressedBuffer.length > len;
    if (useInternalBuffer) {
      zStream.next_out = uncompressedBuffer;
      zStream.next_out_index = 0;
      zStream.avail_out = uncompressedBuffer.length;
      bufferLen = uncompressedBuffer.length;
    } else {
      zStream.next_out = b;
      zStream.next_out_index = off;
      zStream.avail_out = len;
      bufferLen = len;
    }
    do {
      if (zStream.avail_in == 0 && !reachEOF) {
        // if buffer is empty and more input is available, refill it
        zStream.next_in_index = 0;
        synchronized (stream) {
          if (stream.getStreamPosition() != sourcePosition)
            stream.seek(sourcePosition);
          zStream.avail_in = stream.read(zStream.next_in);
          sourcePosition += zStream.avail_in;
        }
        if (zStream.avail_in == -1) {
          zStream.avail_in = 0;
          reachEOF = true;
        }
      }
      err = zStream.inflate(JZlib.Z_NO_FLUSH);
      if (reachEOF && err == JZlib.Z_BUF_ERROR) {
        return -1;
      }
      if (err != JZlib.Z_OK && err != JZlib.Z_STREAM_END) {
        // Bad data. However, don't throw an exception. Instead give callers a chance to read the
        // rest of the buffer. Upon next read call, end of file will be returned (see above).
        LOGGER.warn("Bad data during ZInflaterInputStream decoding. ZStream error: " + err + ", ZStream message: "
            + zStream.msg);
        badDataPosition = sourcePosition;
      }
      if ((reachEOF || err == JZlib.Z_STREAM_END) && zStream.avail_out == bufferLen) {
        return -1;
      }
    } while (zStream.avail_out == bufferLen && err == JZlib.Z_OK);

    if (useInternalBuffer) {
      uncompressedBufferFillDegree = bufferLen - zStream.avail_out;
      uncompressedBufferIndex = 0;
      final int toCopy = Math.min(len, uncompressedBufferFillDegree);

      System.arraycopy(uncompressedBuffer, uncompressedBufferIndex, b, off, toCopy);

      uncompressedBufferIndex += toCopy;
      position += toCopy;
      return toCopy;
    }

    uncompressedBufferFillDegree = uncompressedBufferIndex = 0;
    final int readCount = len - zStream.avail_out;
    position += readCount;
    return readCount;
  }

  @Override
  public void close() throws IOException {
    super.close();
    if (propagateClose && null != stream) {
      stream.close();
    }
    stream = null;
  }

  @Override
  public long getStreamPosition() throws IOException {
    checkClosed();
    return position;
  }

  @Override
  public long length() throws IOException {
    return -1l;
  }

  @Override
  public void seek(long pos) throws IOException {
    checkClosed();

    if (pos < flushedPos) {
      throw new IndexOutOfBoundsException("pos < flushedPos!");
    }
    bitOffset = 0;

    if (pos == position) {
      return;
    }

    if (pos < position) {
      // /////////////
      // CK: Der folgende Workaround funktioniert nicht, da streamPos
      // jeden beliebigen Wert auch hinter EOF haben kann:
      // check if we have the requested position in the buffer
      // long diff = streamPos - pos;
      // if (reachEOF && diff < Integer.MAX_VALUE
      // && diff < uncompressedBufferIndex) {
      // // enough data in the buffer.
      // streamPos = pos;
      // uncompressedBufferIndex -= (diff -1);
      // return;
      // }
      // //////////////
      sourcePosition = 0;
      position = 0;
      reachEOF = false;
      prepareInflater();

      uncompressedBufferIndex = 0;
      uncompressedBufferFillDegree = 0;
    }

    skip(pos - position);
    position = pos;
  }

  @Override
  public long skip(long count) throws IOException {
    if (count <= 0)
      return 0;

    int r;
    final long skipStart = position;
    final byte[] buffer = new byte[(int) min(count, 4096)];
    while (count > 0 && (r = read(buffer, 0, (int) min(buffer.length, count))) > 0) {
      count -= r;
    }
    return position - skipStart;
  }

  @Override
  public long getSizeEstimate() {
    return stream != null ? stream.getSizeEstimate() : 1024;
  }

}

package com.levigo.jadice.document.io;

import java.io.IOException;

public final class ByteArraySeekableInputStream extends SeekableInputStream {
  private final byte[] buf;
  private int bufPos;

  public ByteArraySeekableInputStream(byte[] buf) {
    this.buf = buf;
  }

  @Override
  public long getStreamPosition() throws IOException {
    checkClosed();
    return bufPos;
  }

  @Override
  public void seek(long pos) throws IOException {
    checkClosed();

    if (pos < flushedPos) {
      throw new IndexOutOfBoundsException("pos < flushedPos!");
    }
    bitOffset = 0;

    bufPos = (int) pos;
  }

  @Override
  public long getSizeEstimate() {
    return 0;
  }

  @Override
  public long length() throws IOException {
    return buf.length;
  }

  @Override
  public int read() throws IOException {
    if (bufPos >= buf.length) {
      return -1;
    }
    return buf[bufPos++] & 0xff;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {

    if (b == null || off + len > b.length || (off | len) < 0 || len == 0) {
      throw new IllegalArgumentException();
    }

    if (bufPos == buf.length) {
      return -1;
    }

    final int toRead = Math.min(buf.length - bufPos, len);

    if (toRead > 0) {
      System.arraycopy(buf, bufPos, b, off, toRead);
    }
    bufPos += toRead;

    return toRead;
  }
}

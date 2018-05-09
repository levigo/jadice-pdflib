/**
 *
 */
package com.levigo.jadice.format.pdf.internal.objects;

import java.io.IOException;

import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.document.io.SubfileSeekableInputStream;

public final class WrappingStreamFactory implements IStreamFactory {

  private static long getLength(SeekableInputStream sis) {
    try {
      return sis.length();
    } catch (IOException e) {
      return Long.MAX_VALUE;
    }
  }

  private final SeekableInputStream sis;
  private final long length;

  public WrappingStreamFactory(SeekableInputStream sis) {
    this(sis, getLength(sis));
  }

  public WrappingStreamFactory(SeekableInputStream sis, long length) {
    this.sis = sis;
    this.length = length;
  }

  public SeekableInputStream createStream() {
    return new SubfileSeekableInputStream(sis, 0, length);
  }
}

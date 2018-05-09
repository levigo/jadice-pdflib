package com.levigo.jadice.format.pdf.internal;

import com.levigo.jadice.document.io.ByteArraySeekableInputStream;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.pdf.internal.objects.IStreamFactory;

public class ByteArrayStreamFactory implements IStreamFactory {
  private final byte[] data;

  public ByteArrayStreamFactory(final byte[] data) {
    this.data = data;
  }

  @Override
  public SeekableInputStream createStream() {
    return new ByteArraySeekableInputStream(data);
  }
}

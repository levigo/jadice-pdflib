package com.levigo.jadice.format.pdf.internal.objects;

import com.levigo.jadice.document.io.SeekableInputStream;

public interface IStreamFactory {

  SeekableInputStream createStream();

}

package com.levigo.jadice.format.pdf.internal.parsing;

import java.io.IOException;

import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.ps.internal.TokenProvider;

public interface IPDFLexer extends TokenProvider {

  public Object getLock();

  public long getFilePointer();

  void seek(long target) throws IOException;

  SeekableInputStream getDocumentStream();

  /**
   * read a single byte at the current stream position. This operation should be used rarely and
   * with extreme caution. Peeking tokens before prevents this method to work correct. Due to this,
   * a {@link IllegalStateException} will be throw in case token have been peeked and not yet been
   * consumed.
   *
   * @return
   * @throws IOException
   * @throws IllegalStateException
   */
  int read() throws IOException, IllegalStateException;
  // public long getCrossReferenceLocation() throws IOException;
}

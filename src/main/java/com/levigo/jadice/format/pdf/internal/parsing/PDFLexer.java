package com.levigo.jadice.format.pdf.internal.parsing;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.pdf.internal.msg.GlobalMessages;
import com.levigo.jadice.format.ps.internal.Token;


/**
 * core lexer for pdf documents. Keep in mind that this class <b>IS NOT THREAD SAFE!</b>
 * Synchronization has to be done on invocation time using the object returned by getLock() as the
 * lock.
 */
public class PDFLexer extends AbstractPDFLexer implements IPDFLexer {
  protected static final Token TOKEN_OBJECT_BEGIN = new StringToken(TokenTypesPDF.KEYWORD_OBJECT,
      new byte[]{'o', 'b', 'j'
      });
  protected static final Token TOKEN_OBJECT_END = new StringToken(TokenTypesPDF.KEYWORD_ENDOBJECT,
      new byte[]{'e', 'n', 'd', 'o', 'b', 'j'
      });
  protected static final Token TOKEN_STREAM_BEGIN = new StringToken(TokenTypesPDF.KEYWORD_STREAM,
      new byte[]{'s', 't', 'r', 'e', 'a', 'm'
      });
  protected static final Token TOKEN_STREAM_END = new StringToken(TokenTypesPDF.KEYWORD_ENDSTREAM,
      new byte[]{'e', 'n', 'd', 's', 't', 'r', 'e', 'a', 'm'
      });
  private static final Logger LOGGER = LoggerFactory.getLogger(PDFLexer.class);
  private final Object lock;

  /**
   * this initializes the tokenizer with an SeekableInputStream
   *
   * @param is the Stream of the PDF file which will be tokenized
   */
  public PDFLexer(SeekableInputStream is) {
    super(is);
    if (is == null) {
      throw new IllegalArgumentException("the lexer can't be initalized without an SeekableInputStream");
    }
    lock = is;
  }

  public Object getLock() {
    return lock;
  }

  public long getFilePointer() {
    try {
      return lookaheadStream.getStreamPosition();
    } catch (final IOException e) {
      LOGGER.error(GlobalMessages.DOCUMENT_STREAM_ACCESS_FAILED, e);
      return -1;
    }
  }

  public SeekableInputStream getDocumentStream() {
    return lookaheadStream.getSource();
  }

  public int read() throws IOException, IllegalStateException {
    if (getTokenProvider().getQueueSize() != 0) {
      throw new IllegalStateException("peeked elements in token queue. Unable to proceed.");
    }
    return lookaheadStream.read();
  }

  @Override
  protected Token nextToken() throws IOException {

    skipWhitespaces();

    final int la = lookaheadStream.lookahead(1);

    // the following keywords (obj, endobj, stream, endstream, trailer,
    // statxref) are hard coded
    // and no array compare as we would have to process the array which
    // would cause the well known array processing overhead

    // FIXME the hardcoded elements startxref, xref, f and n are only required
    // once while parsing the cross reference table. Should we create a
    // "CrossReferenceTableParser" for those?
    if (la == 'o'//
        && lookaheadStream.lookahead(2) == 'b'//
        && lookaheadStream.lookahead(3) == 'j') {
      lookaheadStream.consume(3);
      return TOKEN_OBJECT_BEGIN;
    } else if (la == 'e'//
        && lookaheadStream.lookahead(2) == 'n'//
        && lookaheadStream.lookahead(3) == 'd'//
        && lookaheadStream.lookahead(4) == 'o' //
        && lookaheadStream.lookahead(5) == 'b'//
        && lookaheadStream.lookahead(6) == 'j') {
      lookaheadStream.consume(6);
      return TOKEN_OBJECT_END;
    } else if (la == 's' //
        && lookaheadStream.lookahead(2) == 't' //
        && lookaheadStream.lookahead(3) == 'r'//
        && lookaheadStream.lookahead(4) == 'e'//
        && lookaheadStream.lookahead(5) == 'a'//
        && lookaheadStream.lookahead(6) == 'm') {
      lookaheadStream.consume(6);
      return TOKEN_STREAM_BEGIN;
    } else if (la == 'e' //
        && lookaheadStream.lookahead(2) == 'n' //
        && lookaheadStream.lookahead(3) == 'd' //
        && lookaheadStream.lookahead(4) == 's' //
        && lookaheadStream.lookahead(5) == 't' //
        && lookaheadStream.lookahead(6) == 'r'//
        && lookaheadStream.lookahead(7) == 'e'//
        && lookaheadStream.lookahead(8) == 'a'//
        && lookaheadStream.lookahead(9) == 'm') {
      lookaheadStream.consume(9);
      return TOKEN_STREAM_END;
    } else {
      return super.nextToken();
    }
  }

  byte[] getRegion(int seekBack, int regionSize) {
    final long savedFilePointer = getFilePointer();

    // seek back the region we wanted it to

    final byte[] rawData = new byte[regionSize];

    try {
      seek(savedFilePointer - seekBack);
      for (int i = 0; i < regionSize; i++) {
        rawData[i] = (byte) lookaheadStream.read();
      }
    } catch (final IOException e) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.warn(GlobalMessages.PDFLexer_131, e);
      }
      return null;
    }

    try {
      seek(savedFilePointer);
    } catch (final IOException e) {
      LOGGER.error(GlobalMessages.SEEK_IN_DOCUMENT_FAILED, e);
    }

    return rawData;
  }

}

package com.levigo.jadice.format.pdf.internal.parsing;

import java.io.IOException;

import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.ps.internal.Token;

public class PDFDocumentStructureLexer extends PDFLexer {
  protected static final Token TOKEN_KEYWORD_TRAILER = new StringToken(TokenTypesPDF.TOKEN_TYPE_STRING,
      new byte[]{'t', 'r', 'a', 'i', 'l', 'e', 'r'
      });

  protected static final Token TOKEN_KEYWORD_STARTXREF = new StringToken(TokenTypesPDF.TOKEN_TYPE_STRING,
      new byte[]{'s', 't', 'a', 'r', 't', 'x', 'r', 'e', 'f'
      });

  protected static final Token TOKEN_KEYWORD_XREF = new StringToken(TokenTypesPDF.TOKEN_TYPE_STRING,
      new byte[]{'x', 'r', 'e', 'f'
      });

  protected static final Token TOKEN_KEYWORD_FREE = new StringToken(TokenTypesPDF.TOKEN_TYPE_STRING, new byte[]{'f'
  });

  protected static final Token TOKEN_KEYWORD_USED = new StringToken(TokenTypesPDF.TOKEN_TYPE_STRING, new byte[]{'n'
  });

  public PDFDocumentStructureLexer(SeekableInputStream is) {
    super(is);
  }

  @Override
  protected Token nextToken() throws IOException {
    skipWhitespaces();

    final int la = lookaheadStream.lookahead(1);

    if (la == 't' //
        && lookaheadStream.lookahead(2) == 'r' //
        && lookaheadStream.lookahead(3) == 'a'//
        && lookaheadStream.lookahead(4) == 'i'//
        && lookaheadStream.lookahead(5) == 'l'//
        && lookaheadStream.lookahead(6) == 'e'//
        && lookaheadStream.lookahead(7) == 'r') {
      lookaheadStream.consume(7);
      return TOKEN_KEYWORD_TRAILER;
    } else if (la == 's' //
        && lookaheadStream.lookahead(2) == 't' //
        && lookaheadStream.lookahead(3) == 'a'//
        && lookaheadStream.lookahead(4) == 'r'//
        && lookaheadStream.lookahead(5) == 't'//
        && lookaheadStream.lookahead(6) == 'x'//
        && lookaheadStream.lookahead(7) == 'r'//
        && lookaheadStream.lookahead(8) == 'e'//
        && lookaheadStream.lookahead(9) == 'f'//
        ) {
      lookaheadStream.consume(9);
      return TOKEN_KEYWORD_STARTXREF;
    } else if (la == 'x'//
        && lookaheadStream.lookahead(2) == 'r'//
        && lookaheadStream.lookahead(3) == 'e'//
        && lookaheadStream.lookahead(4) == 'f'//
        ) {
      lookaheadStream.consume(4);
      return TOKEN_KEYWORD_XREF;
    } else if (la == 'f'//
        && isWhitespace(lookaheadStream.lookahead(2))//
        ) {
      lookaheadStream.consume(1);
      return TOKEN_KEYWORD_FREE;
    } else if (la == 'n'//
        && isWhitespace(lookaheadStream.lookahead(2))//
        ) {
      lookaheadStream.consume(1);
      return TOKEN_KEYWORD_USED;
    } else {
      return super.nextToken();
    }
  }

}

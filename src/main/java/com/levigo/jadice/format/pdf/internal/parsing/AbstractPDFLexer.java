package com.levigo.jadice.format.pdf.internal.parsing;

import static com.levigo.jadice.format.pdf.internal.parsing.PDFLexers.hexToBytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.levigo.jadice.document.io.ConcurrentSeekableLookaheadStream;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.document.io.SeekableLookaheadStream;
import com.levigo.jadice.format.pdf.internal.ArrayQueue;
import com.levigo.jadice.format.pdf.internal.objects.DSInteger;
import com.levigo.jadice.format.pdf.internal.objects.DSLiteralString;
import com.levigo.jadice.format.pdf.internal.objects.DSNameObject;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;
import com.levigo.jadice.format.pdf.internal.objects.DSReal;
import com.levigo.jadice.format.ps.internal.Lexer;
import com.levigo.jadice.format.ps.internal.StatefulTokenProvider;
import com.levigo.jadice.format.ps.internal.Token;

public abstract class AbstractPDFLexer extends Lexer {
  protected static final Token TOKEN_KEYWORD_TRUE = new StringToken(TokenTypesPDF.KEYWORD_TRUE,
      new byte[]{'t', 'r', 'u', 'e'
      });
  protected static final Token TOKEN_KEYWORD_FALSE = new StringToken(TokenTypesPDF.KEYWORD_FALSE,
      new byte[]{'f', 'a', 'l', 's', 'e'
      });
  protected static final Token TOKEN_KEYWORD_NULL = new StringToken(TokenTypesPDF.KEYWORD_NULL,
      new byte[]{'n', 'u', 'l', 'l'
      });
  protected static final Token TOKEN_ARRAY_BEGIN = new StringToken(TokenTypesPDF.ARRAY_BEGIN, new byte[]{'['
  });
  protected static final Token TOKEN_ARRAY_END = new StringToken(TokenTypesPDF.ARRAY_END, new byte[]{']'
  });
  protected static final Token TOKEN_DICTIONARY_BEGIN = new StringToken(TokenTypesPDF.DICTIONARY_BEGIN,
      new byte[]{'<', '<'
      });
  protected static final Token TOKEN_DICTIONARY_END = new StringToken(TokenTypesPDF.DICTIONARY_END, new byte[]{'>', '>'
  });
  protected static final Token TOKEN_REF = new StringToken(TokenTypesPDF.REF, new byte[]{'R'
  });
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPDFLexer.class);
  /**
   * PDF32000-1:2008 defines the "Number of significant decimal digits of precision in fractional
   * part" of real numers to be approximately 5.
   */
  private static final int NUMBER_OF_SIGNIFICANT_FRACTIONAL_DIGITS = 7;

  protected static class NumberToken extends Token {
    private final Number num;

    public NumberToken(long tokenType, Number num) {
      super(tokenType);
      this.num = num;
    }

    @Override
    public Number getNumberToken() {
      return num;
    }

    @Override
    public byte[] getStringToken() {
      return null;
    }
  }

  protected static class StringToken extends Token {
    private final byte[] string;

    public StringToken(long tokenType, byte[] data) {
      super(tokenType);
      string = data;
    }

    @Override
    public Number getNumberToken() {
      return null;
    }

    @Override
    public byte[] getStringToken() {
      return string;
    }
  }

  protected class PDFTokenProvider implements StatefulTokenProvider {
    private final ArrayQueue<Token> queue;

    public PDFTokenProvider() {
      queue = new ArrayQueue<>(15);
    }

    @Override
    public Token getNextToken() throws IOException {
      fill(1);
      return queue.poll();
    }

    @Override
    public Token peekToken(int tokenNumber) throws IOException {
      fill(tokenNumber + 1);
      return queue.peek(tokenNumber);
    }

    private void fill(int count) throws IOException {
      final int reuqired = count - queue.size();

      for (int i = 0; i < reuqired; i++) {
        queue.add(nextToken());
      }
    }

    @Override
    public boolean isValid() {
      return true;
    }

    protected void reset() {
      queue.clear();
    }

    public int getQueueSize() {
      return queue.size();
    }
  }

  /**
   * returns whether or not the passed character is a whitespace character as defined in
   * PDF32000-1:2008, 7.2.2 "Character Set", Table 1 "White-space characters"
   *
   * @param c the character to be checked
   * @return <code>true</code> if it is a whitespace character, <code>false</code> otherwise
   */
  public static final boolean isWhitespace(int c) {
    switch (c){
      case 0x00: // Null
      case 0x09: // Horizontal Tab
      case 0x0A: // Line Feed
      case 0x0C: // Form Feed
      case 0x0D: // Carriage Return
      case 0x20: // Space
        return true;

      default:
        return false;
    }
  }

  /**
   * checks if the passed (ASCII encoded) character is either '0-9', '.', '+' or '-'
   *
   * @param c the character to be checked
   * @return true if it is one of the above
   */
  public static final boolean isNumber(int c) {
    return (isNumberDigit(c) || (c == '.') || (c == '-') || (c == '+'));
  }

  /**
   * checks if the passed (ASCII encoded) character is in the range '0-9'
   *
   * @param c the character to be checked
   * @return true if it is one of the above
   */
  public static final boolean isNumberDigit(int c) {
    return ((c >= '0') && (c <= '9'));
  }

  /**
   * checks if the passed character is a word character, which means that it checks if it is in
   * either of the ranges 'a..z' or 'A..Z'
   *
   * @param c the character to be checked
   * @return true if it is an word character
   */
  public static final boolean isWordChar(int c) {
    return (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')));
  }

  /**
   * checks if the passed character is a valid nameobject character. Basically it is any character
   * except whitespaces and all syntactical characters.
   *
   * @param c the character to be checked
   * @return true if the character is a valid nameobject character
   */
  public static final boolean isValidNameObjectCharacter(int c) {
    return (!isWhitespace(c) //
        && !isNameObjectBegin(c) //
        && !isArrayBegin(c) //
        && !isArrayEnd(c) //
        && !isHexStringBegin(c) //
        && !isHexStringEnd(c) // hex string end is needed
        // as '>' indicates also the end of a dictionary
        && !isCommentChar(c) //
        && !isLiteralStringBegin(c) //
    );
  }

  /**
   * checks if the passed character is the opening bracket ('[') indicating the start of an Array
   *
   * @param c the character to be checked
   * @return true if the character is indicating an array opening
   */
  public static final boolean isArrayBegin(int c) {
    return c == '[';
  }

  /**
   * checks if the passed character is the closing bracket (']') indicating the end of an Array
   *
   * @param c the character to be checked
   * @return true if the character is indicating an array ending
   */
  public static final boolean isArrayEnd(int c) {
    return c == ']';
  }

  /**
   * checks if the passed character is the opening parenthesis ('(') indicating the start of a
   * literal string
   *
   * @param c the character to be checked
   * @return true if the character is indicating the beginning
   */
  public static final boolean isLiteralStringBegin(int c) {
    return c == '(';
  }

  /**
   * checks if the passed character is the closing parenthesis (')') indicating the start of a
   * literal string
   *
   * @param c the character to be checked
   * @return true if the character is indicating the ending
   */
  public static final boolean isLiteralStringEnd(int c) {
    return c == ')';
  }

  /**
   * checks if the passed character is the opening squared bracket (' <') indicating the start of an
   * hex string
   *
   * @param c the character to be checked
   * @return true if the character is indicating an the beginning
   */
  public static final boolean isHexStringBegin(int c) {
    return c == '<';
  }

  /**
   * checks if the passed character is the closing squared bracket ('>') indicating the end of an
   * hex string
   *
   * @param c the character to be checked
   * @return true if the character is indicating an the ending
   */
  public static final boolean isHexStringEnd(int c) {
    return c == '>';
  }

  /**
   * checks if the passed character is indicating the end of file
   *
   * @param c the character to be checked
   * @return true if the character is indicating end of file
   */
  public static final boolean isEOF(int c) {
    if (c == -1) {
      return true;
    }
    return false;
  }

  /**
   * checks if the passed character is indicating the beginning of a nameobject ( <code>'/'</code>)
   *
   * @param c the character to be checked
   * @return true if the character is indicating the beginning of a name object
   */
  public static final boolean isNameObjectBegin(int c) {
    return (c == '/');
  }

  /**
   * checks if the passed character is the escaping character <code>\</code>
   *
   * @param c the character to be checked
   * @return true if the character is the escaping character
   */
  public static final boolean isEscapingChar(int c) {
    return (c == '\\');
  }

  /**
   * checks if the passed character is the comment character <code>%</code>
   *
   * @param c the character to be checked
   * @return true if the character is the comment character
   */
  public static final boolean isCommentChar(int c) {
    return (c == '%');
  }

  /**
   * checks if the passed character is a valid hexadecimal digit. That means that is has to be in
   * the following ranges: <br>
   * <code>
   * 0..9<br>
   * A..F<br>
   * a..f<br>
   * </code><br>
   *
   * @param c the character to be checked
   * @return true if the character is a valid hexadecimal digit
   */
  public static final boolean isHexDigit(int c) {
    return ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'));
  }

  /**
   * small helping method to resize an <code>byte[]</code>, as it is a native type and all normal
   * resizing methods use <code>Object</code> as base. The resized array (containing the data of the
   * original array) will be returned
   *
   * @param src     the source <code>byte[]</code>
   * @param newSize the size of the array which will be returned
   * @return the new resized <code>byte[]</code>
   * @throws IndexOutOfBoundsException if the passed size is negative or 0
   */
  protected static byte[] resizeByteArray(byte[] src, int newSize) {
    if (newSize < 1) {
      throw new IndexOutOfBoundsException("the requested size of the new array is less than 1");
    }

    final byte[] newArray = new byte[newSize];
    System.arraycopy(src, 0, newArray, 0, Math.min(newSize, src.length));
    return newArray;
  }

  protected final ConcurrentSeekableLookaheadStream lookaheadStream;
  private PDFTokenProvider tokenProvider;

  protected AbstractPDFLexer(SeekableInputStream source) {
    super(false); // no tokenprovider validation needed
    lookaheadStream = new ConcurrentSeekableLookaheadStream(source, 1024);
  }

  @Override
  protected PDFTokenProvider getTokenProvider() {
    if (tokenProvider == null) {
      tokenProvider = new PDFTokenProvider();
    }
    return tokenProvider;
  }

  @Override
  public final void seek(long pos) throws IOException {
    getTokenProvider().reset();
    lookaheadStream.seek(pos);
  }

  protected Token nextToken() throws IOException {
    skipWhitespaces();

    int la = lookaheadStream.lookahead(1);
    if (isArrayBegin(la)) {
      lookaheadStream.consume(1);
      return TOKEN_ARRAY_BEGIN;
    } else if (isArrayEnd(la)) {
      lookaheadStream.consume(1);
      return TOKEN_ARRAY_END;
    } else if (isLiteralStringBegin(la)) {
      // handle literal string processing
      return nextLiteralString();
    } else if (isNumber(la)) {
      // handle number processing
      return nextNumber();

    } else if (isHexStringBegin(la)) {
      if (isHexStringBegin(lookaheadStream.lookahead(2))) {
        lookaheadStream.consume(2);
        return TOKEN_DICTIONARY_BEGIN;
      }
      return nextHexString();
    } else if (isNameObjectBegin(la)) {
      // handle name object processing
      return nextNameObject();

    } else if (isHexStringEnd(la)) {
      if (AbstractPDFLexer.isHexStringEnd(lookaheadStream.lookahead(2))) {
        // null indicates the end of the dictionary
        lookaheadStream.consume(2);
        return TOKEN_DICTIONARY_END;
      } else {
        throw new IOException("unrecognized sequence");
      }
    } else if (la == 'R') {

      lookaheadStream.consume(1);
      return TOKEN_REF;

    } else if (la == 'f'//
        && lookaheadStream.lookahead(2) == 'a'//
        && lookaheadStream.lookahead(3) == 'l'//
        && lookaheadStream.lookahead(4) == 's'//
        && lookaheadStream.lookahead(5) == 'e'//
        ) {
      lookaheadStream.consume(5);
      return TOKEN_KEYWORD_FALSE;
    } else if (la == 't'//
        && lookaheadStream.lookahead(2) == 'r'//
        && lookaheadStream.lookahead(3) == 'u'//
        && lookaheadStream.lookahead(4) == 'e'//
        ) {
      lookaheadStream.consume(4);
      return TOKEN_KEYWORD_TRUE;
    } else if (la == 'n'//
        && lookaheadStream.lookahead(2) == 'u'//
        && lookaheadStream.lookahead(3) == 'l'//
        && lookaheadStream.lookahead(4) == 'l'//
        ) {
      lookaheadStream.consume(4);
      return TOKEN_KEYWORD_NULL;
    } else if (isCommentChar(la)) {
      while ((la = lookaheadStream.lookahead(1)) != -1 && la != '\n' && la != '\r')
        lookaheadStream.consume(1);
      return nextToken();
    } else if (isEOF(la)) {
      return EOF_TOKEN;
    } else {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn("illegal content sequence at offset {0}. Hex value of content sequence: 0x{1}",
            lookaheadStream.getStreamPosition(),
            Integer.toHexString(la));
      }
      return EOF_TOKEN;
    }

  }

  protected void skipWhitespaces() throws IOException {
    while (isWhitespace(lookaheadStream.lookahead(1))) {
      lookaheadStream.consume(1);
    }
  }

  /**
   * parses an hex string based on the PDF lexical rules
   *
   * @throws IOException in case of I/O Errors
   */
  protected Token nextHexString() throws IOException {

    lookaheadStream.consume(1);

    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int c;
    while (!isHexStringEnd(c = lookaheadStream.read()) && !isEOF(c)) {

      // only respect hex legal characters
      // if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
      // || (c >= '0' && c <= '9')) {
      if (isHexDigit(c)) {
        buffer.write(c);
      } else if (!isWhitespace(c)) {
        if (LOGGER.isWarnEnabled()) {
          LOGGER.warn("invalid character at offset {0}. Hex value of character: 0x{1}",
              lookaheadStream.getStreamPosition(),
              Integer.toHexString(c));
        }
      }
    }

    if ((buffer.size() % 2) != 0) {
      buffer.write('0');
    }

    return new StringToken(TokenTypesPDF.HEX_STRING, (hexToBytes(buffer.toByteArray())));
  }

  /**
   * parses an literal string based on the PDF lexical rules. the character beeing passed HAS to be
   * the opening parenthesis!
   *
   * @return the parsed {@link DSLiteralString}
   * @throws IOException in case of I/O Errors
   */
  protected StringToken nextLiteralString() throws IOException {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    // if we're here we have at least one parenthesis
    int parenthesisCount = 1;

    lookaheadStream.consume(1);

    // loop until we've got a final parenthesis
    do {
      int la = lookaheadStream.lookahead(1);

      if (isEOF(la)) {
        throw new IOException("premature end of pdf document data");
      } else if (isLiteralStringBegin(la)) {
        parenthesisCount++;
        buffer.write(la);
        lookaheadStream.consume(1);
      } else if (isLiteralStringEnd(la)) {
        parenthesisCount--;
        // only append the ) if it is not the closing one.
        if (parenthesisCount > 0)
          buffer.write(la);

        lookaheadStream.consume(1);
      } else if (isEscapingChar(la)) {
        if (isNumberDigit(lookaheadStream.lookahead(2))) {
          int number = 0;
          lookaheadStream.consume(1);
          for (int i = 0; i < 3 && AbstractPDFLexer.isNumberDigit(la = lookaheadStream.lookahead(1)); i++) {
            number *= 8;
            number += (la - '0');
            lookaheadStream.consume(1);
          }
          buffer.write(number);
        } else if (isLiteralStringBegin(lookaheadStream.lookahead(2)) || isLiteralStringEnd(
            lookaheadStream.lookahead(2))) {
          buffer.write(lookaheadStream.lookahead(2));
          lookaheadStream.consume(2);
        } else {
          int consumeCount = 2;
          switch (lookaheadStream.lookahead(2)){
            // escape sequences in the string
            case 'n':
              buffer.write('\n');
              break;
            case 'r':
              buffer.write('\r');
              break;
            case 't':
              buffer.write('\t');
              break;
            case 'b':
              buffer.write('\b');
              break;
            case 'f':
              buffer.write('\f');
              break;
            case '\\':
              buffer.write('\\');
              break;

            // ignoring of escape sequences
            case '\n':
            case '\r':
              // nothing to do as the \n and \r have been escaped. So they have to be ignored
              break;

            default:
              // We have an incomplete escape sequence. Acrobat discards the 'SOLIDUS' character in
              // such cases.
              consumeCount = 1;
              // JSX-1781 Fix: Don't write back a corrupt escape sequence
              // buffer.write('\\');
          }
          lookaheadStream.consume(consumeCount);
        }
      } else {
        buffer.write(la);
        lookaheadStream.consume(1);
      }
    } while (parenthesisCount > 0);

    return new StringToken(TokenTypesPDF.LITERAL_STRING, buffer.toByteArray());
  }

  /**
   * parses a name object based on the PDF lexical rules
   *
   * @return the parsed {@link DSNameObject}
   * @throws IOException in case of I/O Errors
   */
  protected Token nextNameObject() throws IOException {
    final ByteArrayOutputStream buff = new ByteArrayOutputStream();

    lookaheadStream.consume(1);
    // as there are so many ugly pdf files we've got to make
    // serveral checks here
    int c;
    while (!isEOF(c = lookaheadStream.lookahead(1)) // is it EOF?
        && !isWhitespace(c) // no whitespaces
        && !isArrayBegin(c) // array following?
        && !isArrayEnd(c) // array following?
        && !isHexStringBegin(c) // hex string following
        && !isHexStringEnd(c) // hex string following
        && !isLiteralStringBegin(c) // literal string following?
        && !isNameObjectBegin(c) // is a name object following?
        && !isCommentChar(c) // some comment?
        ) {
      if (c == '#') {
        final int p1 = lookaheadStream.lookahead(2);
        final int p2 = lookaheadStream.lookahead(3);
        if (isHexDigit(p1) && isHexDigit(p2)) {

          buff.write(hexToBytes(new byte[]{(byte) p1, (byte) p2
          }));
          lookaheadStream.consume(3);

        } else {
          // not a valid hex string. Appending the #-Character and proceeding
          buff.write('#');
          lookaheadStream.consume(1);
        }
      } else {
        lookaheadStream.consume(1);
        buff.write(c);
      }
    }

    return new StringToken(TokenTypesPDF.NAME, buff.toByteArray());
  }

  /**
   * parses Number based on the PDF lexical rules
   *
   * @return the parsed {@link DSObject}but in reality it is either a instance of {@link DSInteger}
   * or {@link DSReal}
   * @throws IOException in case of I/O Errors
   */
  protected NumberToken nextNumber() throws IOException {
    long value = 0;
    int postPeriodCount = 0;
    boolean negative = false;
    boolean seenDot = false;

    int la = lookaheadStream.lookahead(1);
    if (la == '+') {
      lookaheadStream.consume(1);
    } else if (la == '-') {
      lookaheadStream.consume(1);
      negative = true;
    }
    while (isNumberDigit(la = lookaheadStream.lookahead(1)) || la == '.') {
      // parsing too many fractional digits will lead to overflow because of multiplication by 10
      if (postPeriodCount < NUMBER_OF_SIGNIFICANT_FRACTIONAL_DIGITS) {
        if (la >= '0' && la <= '9') {
          value *= 10;
          value += la - '0';
          // if there was a dot seen
          if (seenDot) {
            postPeriodCount++;
          }
        } else if (la == '.') {
          seenDot = true;
        } else if (la == ',') {
          // ignore, but check just to be sure
          // it is really ignored
        }
      }
      lookaheadStream.consume(1);
    }
    if (seenDot) {
      double result;
      double divider = 1;
      for (int i = 1; i <= postPeriodCount; ++i) {
        divider *= 10;
      }
      result = (value) / divider;
      if (negative) {
        result = -result;
      }
      return new NumberToken(TokenTypesPDF.REAL, new Double(result));
    }

    if (negative) {
      value = -value;
    }

    // if there was no dot we return an integer
    return new NumberToken(TokenTypesPDF.INT, new Long(value));
  }

  @Override
  public SeekableLookaheadStream getLookaheadStream() {
    return lookaheadStream;
  }
}

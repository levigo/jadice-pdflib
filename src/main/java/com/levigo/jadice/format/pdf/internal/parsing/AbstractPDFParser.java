package com.levigo.jadice.format.pdf.internal.parsing;

import static org.jadice.util.base.Strings.utf8String;

import static com.levigo.jadice.format.pdf.internal.objects.DS.*;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.levigo.jadice.format.pdf.crypt.PDFSecurityException;
import com.levigo.jadice.format.pdf.internal.crypt.NoSecurityHandler;
import com.levigo.jadice.format.pdf.internal.crypt.SecurityHandler;
import com.levigo.jadice.format.pdf.internal.crypt.StringDecryptor;
import com.levigo.jadice.format.pdf.internal.objects.DSArray;
import com.levigo.jadice.format.pdf.internal.objects.DSBoolean;
import com.levigo.jadice.format.pdf.internal.objects.DSCommonDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSHexString;
import com.levigo.jadice.format.pdf.internal.objects.DSLiteralString;
import com.levigo.jadice.format.pdf.internal.objects.DSNameObject;
import com.levigo.jadice.format.pdf.internal.objects.DSNullObject;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;
import com.levigo.jadice.format.pdf.internal.objects.DSReal;
import com.levigo.jadice.format.ps.internal.Token;
import com.levigo.jadice.format.ps.internal.TokenProvider;
import com.levigo.jadice.format.ps.internal.TokenTypes;


public abstract class AbstractPDFParser extends AbstractParserSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPDFParser.class);

  protected final TokenProvider lexer;
  protected SecurityHandler securityHandler;

  public AbstractPDFParser(TokenProvider lexer) {
    this(lexer, new NoSecurityHandler());
  }

  public AbstractPDFParser(TokenProvider lexer, SecurityHandler securityHandler) {
    this.lexer = lexer;
    this.securityHandler = securityHandler;
  }

  protected DSObject parseObjectInternal(long objectNumber, int generationNumber) throws IOException {
    return parseObjectInternal(lexer.getNextToken(), objectNumber, generationNumber);
  }

  protected DSObject parseObjectInternal(Token token, long objectNumber, int generationNumber) throws IOException {
    if (token.isOfType(TokenTypes.TOKEN_TYPE_STRING)) {

      // complex elements
      if (token.isOfType(TokenTypesPDF.DICTIONARY_BEGIN)) {
        return parseDictionary(objectNumber, generationNumber);
      }

      if (token.isOfType(TokenTypesPDF.ARRAY_BEGIN)) {
        return parseArray(objectNumber, generationNumber);
      }

      // simpler elements
      if (token.isOfType(TokenTypesPDF.NAME)) {
        return parseName(token);
      }

      if (token.isOfType(TokenTypesPDF.LITERAL_STRING)) {
        try {
          final DSLiteralString srcString = new DSLiteralString(token.getStringToken());
          final StringDecryptor stringDecryptor = securityHandler.getStringDecryptor();
          return stringDecryptor.getDecryptedString(srcString, objectNumber, generationNumber);
        } catch (final PDFSecurityException e) {
          if (LOGGER.isErrorEnabled())
            LOGGER.error("String decryption failed.", e);

          return new DSLiteralString(token.getStringToken());
        }
      }

      if (token.isOfType(TokenTypesPDF.HEX_STRING)) {
        try {
          final DSHexString srcString = new DSHexString(token.getStringToken());
          final StringDecryptor stringDecryptor = securityHandler.getStringDecryptor();
          return stringDecryptor.getDecryptedString(srcString, objectNumber, generationNumber);
        } catch (final PDFSecurityException e) {
          if (LOGGER.isErrorEnabled())
            LOGGER.error("String decryption failed.", e);

          return new DSHexString(token.getStringToken());
        }
      }

      // special keywords (true, false, null)
      if (token.isOfType(TokenTypesPDF.KEYWORD_TRUE)) {
        return DSBoolean.TRUE;
      }

      if (token.isOfType(TokenTypesPDF.KEYWORD_FALSE)) {
        return DSBoolean.FALSE;
      }

      if (token.isOfType(TokenTypesPDF.KEYWORD_NULL)) {
        return DSNullObject.INSTANCE;
      }

      // best effort: Use UTF-8 since we don't know which encoding to use (or if the unrecognized
      // token will be a valid string at all).
      throw new RuntimeException("found unknown syntactical element: '" + utf8String(token.getStringToken()) + "'");
    }

    if (token.isOfType(TokenTypes.TOKEN_TYPE_NUMBER)) {

      // simple case. We only have a single entry, which is a number
      if (token.isOfType(TokenTypesPDF.INT)) {

        // check if we have a reference
        if (lexer.peekToken(0).isOfType(TokenTypesPDF.INT) && lexer.peekToken(1).isOfType(TokenTypesPDF.REF)) {

          final int genNum = lexer.getNextToken().getNumberToken().intValue();
          // just remove the trailing 'R' token
          lexer.getNextToken();

          return ref(token.getNumberToken().longValue(), genNum);
        }

        return integer(token.getNumberToken().longValue());
      }

      // it may only be a real if it hasn't been an int
      return new DSReal(token.getNumberToken().doubleValue());

    }

    throw new RuntimeException("found unexpected syntactical element while reading");
  }

  /**
   * immediate parsing of an array. It is assumed that the array start {@link Token} ('
   * <code>[</code>') has already been processed.
   *
   * @param generationNumber
   * @param objectNumber
   * @return
   * @throws IOException
   * @throws PDFSecurityException
   */
  protected DSArray parseArray(long objectNumber, int generationNumber) throws IOException {

    final DSArray a = new DSArray();
    Token t;

    while (!(t = lexer.getNextToken()).isOfType(TokenTypesPDF.ARRAY_END) && !t.isOfType(TokenTypes.TOKEN_TYPE_EOF)) {

      a.add(parseObjectInternal(t, objectNumber, generationNumber));

    }

    return a;
  }

  /**
   * immediate parsing of a dictionary. It is assumed that the dictionary start {@link Token} ('
   * <code>&lt;&lt;</code>') has already been processed.
   *
   * @param generationNumber
   * @param objectNumber
   * @return
   * @throws IOException
   * @throws PDFSecurityException
   */
  public DSDictionary parseDictionary(long objectNumber, int generationNumber) throws IOException {
    final DSDictionary d = new DSCommonDictionary();
    Token t;
    while (!(t = lexer.getNextToken()).isOfType(TokenTypesPDF.DICTIONARY_END) && !t.isOfType(
        TokenTypes.TOKEN_TYPE_EOF)) {

      d.addNamedEntry( //
          parseName(t), //
          parseObjectInternal(objectNumber, generationNumber) //
      );
    }
    return d;
  }

  protected DSNameObject parseName(Token t) {
    assertTokenType(t, TokenTypesPDF.NAME);
    return name(t.getStringToken());
  }

  protected Number parseInteger() throws IOException {
    final Token objectNumberToken = lexer.getNextToken();
    assertTokenType(objectNumberToken, TokenTypesPDF.INT);
    final Number num = objectNumberToken.getNumberToken();
    return num;
  }

}

package com.levigo.jadice.format.pdf.internal.parsing;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.document.io.SubfileSeekableInputStream;
import com.levigo.jadice.format.pdf.crypt.PDFSecurityException;
import com.levigo.jadice.format.pdf.internal.DefaultReferenceResolver;
import com.levigo.jadice.format.pdf.internal.PDFFilterFactory;
import com.levigo.jadice.format.pdf.internal.crypt.SecurityHandler;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSInteger;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;
import com.levigo.jadice.format.pdf.internal.objects.DSReference;
import com.levigo.jadice.format.pdf.internal.objects.DSStream;
import com.levigo.jadice.format.pdf.internal.objects.IStreamFactory;
import com.levigo.jadice.format.pdf.internal.parsing.IObjectLocator.Location;
import com.levigo.jadice.format.ps.internal.Token;

public class PDFParser extends AbstractPDFParser implements IPDFParser {
  private static final Logger LOGGER = LoggerFactory.getLogger(PDFParser.class);

  protected static final class StreamFactory implements IStreamFactory {
    private final SeekableInputStream source;
    private final long start;
    private final long length;

    private StreamFactory(SeekableInputStream source, long start, long length) {
      super();
      this.source = source;
      this.start = start;
      this.length = length;
    }

    @Override
    public SeekableInputStream createStream() {
      return new SubfileSeekableInputStream(source, start, length);
    }
  }

  protected final IPDFLexer lexer;
  private final IObjectLocator locator;
  private final boolean hasObjectStreamContext;

  private PDFFilterFactory filterFactory;

  public PDFParser(IPDFLexer lexer, IObjectLocator locator, SecurityHandler securityHandler,
      PDFFilterFactory filterFactory, boolean objectStreamContext) {
    super(lexer);
    this.lexer = lexer;
    this.locator = locator;
    this.securityHandler = securityHandler;
    this.filterFactory = filterFactory;
    hasObjectStreamContext = objectStreamContext;
  }

  public PDFParser(IPDFLexer lexer, IObjectLocator locator, SecurityHandler securityHandler,
      PDFFilterFactory filterFactory) {
    this(lexer, locator, securityHandler, filterFactory, false);
  }

  public PDFParser(IPDFLexer lexer, IObjectLocator locator, SecurityHandler securityHandler) {
    // Workaround to be able to use 'this'. First pass 'null', then create the filter factory manually. 
    this(lexer, locator, securityHandler, null, false);
    filterFactory = new PDFFilterFactory(new DefaultReferenceResolver(this), securityHandler);
  }

  @Override
  public DSObject parseObjectAt(long offset) throws IOException, RuntimeException {
    lexer.seek(offset);

    return parseObject();
  }

  /**
   * parse an object at the current lexer position. This method should be used rarely and is only of
   * use for {@link PDFDocumentStructureParser}
   *
   * @return
   * @throws IOException
   * @throws PDFSecurityException
   */
  public DSObject parseObject() throws IOException {
    // read the object head consisting of object number, generation number and the obj keyword
    final long objectNumber = parseInteger().longValue();
    final int generationNumber = parseInteger().intValue();
    assertTokenType(lexer.getNextToken(), TokenTypesPDF.KEYWORD_OBJECT);

    return parseObjectBody(objectNumber, generationNumber);
  }

  /**
   * this will parse the object body at the current location.
   *
   * @param objectNumber
   * @param generationNumber
   * @return
   * @throws IOException
   * @throws PDFSecurityException
   */
  public DSObject parseObjectBody(final long objectNumber, final int generationNumber) throws IOException {
    // here comes the fun part. Parse the actual object body
    Token token = lexer.getNextToken();

    final DSObject o;
    // start with the most common case: a dictionary declared as an indirect object. This is a
    // shortcut to make processing faster. The same will be declared in parseObject
    if (token.isOfType(TokenTypesPDF.DICTIONARY_BEGIN)) {
      o = parseDictionary(objectNumber, generationNumber);
    } else {
      o = parseObjectInternal(token, objectNumber, generationNumber);
    }

    if (o == null) {
      // parsing failed. This my be due to an error. The object parsing logic has emitted a message.
      return null;
    }

    // apply object reference data
    o.setThisObjectsObjectNumber(objectNumber);
    o.setThisObjectsGenerationNumber(generationNumber);

    // check if this object is a stream
    token = lexer.getNextToken();

    if (token.isOfType(TokenTypesPDF.KEYWORD_STREAM)) {
      // handle stream data.

      if (!(o instanceof DSDictionary)) {
        throw new RuntimeException("illegal content: stream object has not dictionary.");
      }
      final DSDictionary dict = (DSDictionary) o;

      // store the current offset
      long streamBegin = lexer.getFilePointer();

      int c = lexer.read();

      // Heuristic for DOCPV-66
      boolean whitespaceAfterStream = false;
      while (c == ' ') {
        c = lexer.read();
        streamBegin += 1;
        whitespaceAfterStream = true;
      }

      if (whitespaceAfterStream) {
        LOGGER.warn(
            "The stream keyword is followed by space character(s). It must be terminated by either CARRIAGE RETURN and LINE FEED, nor a single LINE FEED");
      }
      // end of heuristic for DOCPV-66

      if (c == '\n') {
        streamBegin += 1;
      } else if (c == '\r') {
        if (lexer.read() == '\n') {
          streamBegin += 2;
        } else {
          LOGGER.warn("stream keyword has been terminated with CARRIAGE RETURN without LINE FEED.");
          streamBegin += 1;
        }
      } else {
        LOGGER.warn(
            "stream keyword has not been terminated by either CARRIAGE RETURN and LINE FEED, nor a single LINE FEED");
      }

      // find the stream length
      DSObject len = dict.getNamedEntryValue("Length");
      if (len instanceof DSReference) {
        final DSReference r = (DSReference) len;
        len = parseObject(r.getReferencedObjectNumber(), r.getReferencedGenerationNumber());
      }

      if (len == null) {
        throw new RuntimeException("missing required length for a stream object.");
      }

      if (!(len instanceof DSInteger)) {
        throw new RuntimeException(
            "stream length was of incorrect type. Expected: DSInteger, was: '" + len.getClass().getSimpleName() + "'");
      }

      final long streamLength = ((DSInteger) len).getLong();


      // jump to the end of the stream object
      lexer.seek(streamBegin + streamLength);

      try {
        if (!lexer.getNextToken().isOfType(TokenTypesPDF.KEYWORD_ENDSTREAM))
          LOGGER.warn("Missing endstream keyword after stream data");
      } catch (final Exception e) {
        LOGGER.error("Missing endstream keyword after stream data", e);
      }

      try {
        if (!lexer.getNextToken().isOfType(TokenTypesPDF.KEYWORD_ENDOBJECT))
          LOGGER.warn("Missing endobj keyword after body of object {0} {1}", objectNumber, generationNumber);
      } catch (final Exception e) {
        LOGGER.error("Missing endobj keyword after body of object {0} {1}", objectNumber, generationNumber, e);
      }

      final DSStream dsStream = new DSStream(dict,
          new StreamFactory(lexer.getDocumentStream(), streamBegin, streamLength));
      dsStream.setThisObjectsObjectNumber(objectNumber);
      dsStream.setThisObjectsGenerationNumber(generationNumber);
      return dsStream;
    } else if (!hasObjectStreamContext && !token.isOfType(TokenTypesPDF.KEYWORD_ENDOBJECT)) {
      if (!lexer.getNextToken().isOfType(TokenTypesPDF.KEYWORD_ENDOBJECT))
        LOGGER.warn("Missing endobj keyword after body of object {0} {1}", objectNumber, generationNumber);
    }
    return o;
  }

  @Override
  public DSObject parseObject(long objectNumber, int generationNumber) throws IOException {
    final Location location = locator.locate(objectNumber, generationNumber);
    return parseObject(objectNumber, generationNumber, location, true);
  }

  private DSObject parseObject(long objectNumber, int generationNumber, Location location, boolean useHeuristic)
      throws IOException {

    DSObject res = null;
    if (location != null) {

      if (location.isNested()) {
        final DSObject containerObject = parseObject(location.getSurroundingObjectNumber(), 0);
        if (containerObject != null && containerObject instanceof DSStream) {

          // try {
          // final DSStream streamObject = decryptionHandler.getDecryptedStream((DSStream)
          // containerObject);
          final DSStream streamObject = (DSStream) containerObject;

          // dump(streamObject, filterFactory);

          final ObjectStreamParser objStrmParser = new ObjectStreamParser(streamObject, locator, filterFactory);

          res = objStrmParser.parse(objectNumber, location.getObjectIndex());
          // } catch (PDFSecurityException e) {
          // log.error(PDFSecurityMessages.DECRYPTION_OF_STREAM_FAILED, e);
          // }

        }
      } else {
        res = parseObjectAt(location.getOffset());
      }
    }

    if (useHeuristic) {

      if (res == null || res.objectNumber() != objectNumber) {
        if (objectNumber > 0)
          res = parseObject(objectNumber, generationNumber, res, -1);

        if (res == null || res.objectNumber() != objectNumber)
          res = parseObject(objectNumber, generationNumber, res, +1);

        if (res != null && res.objectNumber() != objectNumber) {
          LOGGER.warn("Incorrectly formulated cross reference table. Found object {2} {3} instead of object {0} {1}",
              objectNumber, generationNumber, res.objectNumber(),
              res.generationNumber());
        }
      }

    }

    return res;
  }

  // private void dump(DSStream streamObject, PDFFilterFactory filterFactory) {
  // try {
  //
  // int read;
  // final SeekableInputStream inputStream =
  // filterFactory.getInputStreamFromPDFStream(streamObject);
  // while ((read = inputStream.read()) != -1)
  // System.out.print((char) read);
  //
  // } catch (IOException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // }

  private DSObject parseObject(long objectNumber, int generationNumber, DSObject res, int delta) throws IOException {
    Location location;
    if (res != null)
      LOGGER.warn("Incorrectly formulated cross reference table. Found object {2} {3} instead of object {0} {1}",
          objectNumber, generationNumber, res.objectNumber(),
          res.generationNumber());

    location = locator.locate(objectNumber + delta, generationNumber);

    if (location != null)
      res = parseObject(objectNumber, generationNumber, location, false);

    return res;
  }

}

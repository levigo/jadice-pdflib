package com.levigo.jadice.format.pdf.internal.parsing;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.levigo.jadice.document.io.ConcurrentSeekableLookaheadStream;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.document.io.SeekableLookaheadStream;
import com.levigo.jadice.format.pdf.internal.PDFFilterFactory;
import com.levigo.jadice.format.pdf.internal.PDFFormatVersionInfo;
import com.levigo.jadice.format.pdf.internal.crypt.NoSecurityHandler;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;
import com.levigo.jadice.format.pdf.internal.objects.DSStream;
import com.levigo.jadice.format.pdf.internal.parsing.IObjectLocator.Location;
import com.levigo.jadice.format.pdf.internal.struct.CrossReferenceStreamDictionary;
import com.levigo.jadice.format.pdf.internal.struct.CrossReferenceStreamDictionary.Subsection;
import com.levigo.jadice.format.pdf.internal.struct.Trailer;
import com.levigo.jadice.format.ps.internal.Token;
import com.levigo.jadice.format.ps.internal.TokenTypes;

public class PDFDocumentStructureParser extends AbstractParserSupport {
  /**
   * the maximum search range within which the PDF document tail will be searched in
   */
  protected static final int SEARCH_RANGE = 2048;
  protected static final int EOF_MARKER_SEARCH_RANGE = 2048;
  private static final Logger LOGGER = LoggerFactory.getLogger(PDFDocumentStructureParser.class);

  public static final class PDFDocumentStructure {
    private final PDFFormatVersionInfo version;
    private final long length;
    private final IncrementalUpdate activeUpdate;

    public PDFDocumentStructure(PDFFormatVersionInfo version, long length, IncrementalUpdate activeUpdate) {
      super();
      this.version = version;
      this.length = length;
      this.activeUpdate = activeUpdate;
    }

    public IncrementalUpdate getActiveUpdate() {
      return activeUpdate;
    }

    public long getLength() {
      return length;
    }

    public PDFFormatVersionInfo getVersion() {
      return version;
    }

  }

  public static final class IncrementalUpdate {
    private final CrossReferenceTable crossReferenceTable;
    private final Trailer trailer;
    private IncrementalUpdate previous;

    public IncrementalUpdate(CrossReferenceTable crossReferenceTable, Trailer trailer) {
      super();
      this.crossReferenceTable = crossReferenceTable;
      this.trailer = trailer;
    }

    public IncrementalUpdate getPrevious() {
      return previous;
    }

    protected void setPrevious(IncrementalUpdate previous) {
      this.previous = previous;
    }

    public CrossReferenceTable getCrossReferenceTable() {
      return crossReferenceTable;
    }

    public Trailer getTrailer() {
      return trailer;
    }
  }

  protected static final class CrossReferenceParseData {
    private final CrossReferenceTable crossReferenceTable;
    private final Trailer trailer;

    private CrossReferenceParseData(CrossReferenceTable crossReferenceTable, Trailer trailer) {
      super();
      this.crossReferenceTable = crossReferenceTable;
      this.trailer = trailer;
    }

    public CrossReferenceTable getCrossReferenceTable() {
      return crossReferenceTable;
    }

    public Trailer getTrailer() {
      return trailer;
    }
  }

  private final SeekableInputStream sis;
  private final PDFFilterFactory filterFactory;

  public PDFDocumentStructureParser(SeekableInputStream sis, PDFFilterFactory filterFactory) {
    this.sis = sis;
    this.filterFactory = filterFactory;
  }

  /**
   * @throws IOException
   */
  protected long determineFileLength(SeekableInputStream sis) throws IOException {

    synchronized (sis) {

      long filelength = sis.length();

      filelength = verifyLength(sis, filelength);

      if (filelength <= 0) {
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("the length of the source stream couldn't be determined. "
              + "Trying to get the length by reading over the complete stream");
        }

        // it is possible that the filelength can't be
        // determined. Let's try to do an read over the
        // complete stream to get the full length
        long lengthRead = sis.getStreamPosition();

        if (lengthRead < 0)
          // this should not happen, but we're prepared for the case of a defect
          // SeekableInputStream implementation
          lengthRead = 0;

        if (lengthRead > 0) {
          // there have already been some read instructions on the the stream.
          // Trying to resume from this point.

          // check if the next read does return something != -1 to be sure that
          // the filepointer is not pointing beyond the stream end (due to an
          // extreme seek())
          if (sis.read() < 0) {

            // the position is either at the file end or beyond. Resuming from the
            // beginning of the stream
            sis.seek(0);
            lengthRead = 0;
          } else {
            lengthRead += 1;
          }
        }

        final byte[] buf = new byte[3000];
        int r = 0;
        while ((r = sis.read(buf, 0, buf.length)) >= 0) {
          lengthRead += r;
        }

        if (lengthRead <= 0) {
          // even a read over the complete file was unsuccessful
          // we've got break here
          throw new RuntimeException("failed to determine stream length");
        }

        filelength = lengthRead;
      }
      return filelength;
    }
  }

  protected long verifyLength(SeekableInputStream sis, long filelength) throws IOException {
    if (filelength > 0) {
      // DOIP-85: Check if the filelength is correct.
      sis.seek(filelength - 1);
      // this may seem weird but is as it should be. If either the first read fails, or after the
      // first being successful and the second one returns a non EOF result, too: the length is
      // incorrect. We are either over or not at the end.
      if (sis.read() == -1 || sis.read() != -1)
        filelength = -1;

      sis.seek(0);
    }
    return filelength;
  }

  /**
   * @param sis
   * @param fl  the file length
   * @return
   * @throws IOException
   */
  protected long readMainXRefTableLoc(SeekableInputStream sis, long fl) throws IOException {

    // initialize the regions to search in
    final int searchRange = calculateSearchRange(fl);
    final int eofMarkerSearchRange = calculateEOFMarkerSearchRange(searchRange);

    final SeekableLookaheadStream slr = new ConcurrentSeekableLookaheadStream(sis, searchRange);

    // seek to the end of the file -1
    slr.seek(fl - searchRange);

    final int eofMarkerBegin = findEOFMarkerBegin(slr, searchRange, eofMarkerSearchRange);

    long xRefTableOffset;

    if (eofMarkerBegin != -1) {
      // we have a valid %%EOF file ending. continue with backwards-searching
      // the
      // startxref keyword and offset
      xRefTableOffset = findMainXRefTableLocation(slr, searchRange, eofMarkerBegin);
    } else {
      xRefTableOffset = findMainXRefTableLocationFallback(slr, searchRange);
    }

    if (xRefTableOffset > fl || xRefTableOffset <= 0) {
      throw new RuntimeException("Illegal offset of cross reference table (" + xRefTableOffset + ")");
    }

    return xRefTableOffset;
  }

  /**
   * This method is used if the <code>%%EOF</code> marker has not been found at the end of the file.
   * In this case this method will do a backward-search for <code>startxref</code> and then for
   * forward parse the cross reference table offset
   *
   * @param slr
   * @param searchRange
   * @return
   * @throws IOException
   */
  protected long findMainXRefTableLocationFallback(SeekableLookaheadStream slr, int searchRange) throws IOException {

    // /////////////////////////////////////////////////////////////
    // step 1: backward search the startxref keyword
    // /////////////////////////////////////////////////////////////

    // the search offset
    // we are initializing with searchRange-2 as the startxref keyword has to be
    // followed by at least one whitespace and a number.
    int sOffset = searchRange - 2;
    boolean keywordFound = false;

    // 9 because the startxref keyword is 9 bytes long
    while (sOffset > 9 && //
        // search until keywordFound == true
        !(keywordFound = isStartXRefKeyword(slr, sOffset))) {
      sOffset--;
    }

    if (!keywordFound)
      throw new RuntimeException("no startxref keyword found. Failed to load the document.");

    // /////////////////////////////////////////////////////////////
    // step 2: forward parse the offset
    // /////////////////////////////////////////////////////////////

    long xrefOffset = 0;
    // increment, as rOffset is now pointing to the f of startxref
    sOffset++;

    int c;
    while ((c = slr.lookahead(sOffset)) != -1) {

      sOffset++;

      if (AbstractPDFLexer.isNumber(c)) {
        xrefOffset *= 10;
        xrefOffset += c - '0';
      } else if (AbstractPDFLexer.isWhitespace(c)) {
        if (xrefOffset != 0)
          return xrefOffset;
      } else {
        // XXX should the whitespaceFound flag set to true?
        LOGGER.warn("found unexpected characters. This typically means that there are syntactical problems.");
      }

    }

    if (xrefOffset != 0)
      return xrefOffset;

    throw new RuntimeException("missing cross reference table location.");
  }

  protected long findMainXRefTableLocation(final SeekableLookaheadStream slr, final int searchRange,
      final int eofMarkerBegin) throws IOException, EOFException {
    // read a continuous sequence of numbers
    long xRefTableOffset = 0;
    long multiplier = 1;
    int stepcounter = searchRange - eofMarkerBegin;
    boolean whitespaceFound = false;
    while (stepcounter < searchRange) {

      final int curIdx = searchRange - stepcounter++;
      final int c = slr.lookahead(curIdx);

      if (AbstractPDFLexer.isNumber(c)) {
        if (whitespaceFound) {
          // reset previously read numbers
          xRefTableOffset = 0;
          multiplier = 1;
          whitespaceFound = false;
        }
        xRefTableOffset += (c - '0') * multiplier;
        multiplier *= 10;
      } else if (AbstractPDFLexer.isWhitespace(c)) {
        whitespaceFound = true;
      } else if (isStartXRefKeyword(slr, curIdx)) {
        // finally found the startxref keyword.
        break;
      } else if (c == -1) {
        throw new EOFException("premature end of file");
      } else {
        // XXX should the whitespaceFound flag set to true?
        LOGGER.warn("found unexpected characters. This typically means that there are syntactical problems.");
      }
    }
    return xRefTableOffset;
  }

  protected boolean isStartXRefKeyword(final SeekableLookaheadStream slr, final int curIdx) throws IOException {
    return slr.lookahead(curIdx) == 'f' &&
        // this might be the end of the startxref keyword. If it is, we're done!
        curIdx - 8 > 0 && //
        slr.lookahead(curIdx - 1) == 'e' && //
        slr.lookahead(curIdx - 2) == 'r' && //
        slr.lookahead(curIdx - 3) == 'x' && //
        slr.lookahead(curIdx - 4) == 't' && //
        slr.lookahead(curIdx - 5) == 'r' && //
        slr.lookahead(curIdx - 6) == 'a' && //
        slr.lookahead(curIdx - 7) == 't' && //
        slr.lookahead(curIdx - 8) == 's';
  }

  protected final int calculateEOFMarkerSearchRange(final int searchRange) {
    final int eofMarkerSearchRange = Math.min(searchRange, EOF_MARKER_SEARCH_RANGE);
    return eofMarkerSearchRange;
  }

  protected final int calculateSearchRange(long fl) {
    final int searchRange;
    if (fl > SEARCH_RANGE)
      searchRange = SEARCH_RANGE;
    else
      searchRange = (int) fl;
    return searchRange;
  }

  protected int findEOFMarkerBegin(final SeekableLookaheadStream slr, final int searchRange,
      final int eofMarkerSearchRange) throws IOException, EOFException {
    int stepcounter = 0;
    while (eofMarkerSearchRange > stepcounter) {

      final int curIdx = searchRange - stepcounter++;
      final int c = slr.lookahead(curIdx);
      if (c == 'F') {
        if (curIdx - 3 > 0) {
          if (slr.lookahead(curIdx - 1) == 'O' //
              && slr.lookahead(curIdx - 2) == 'E' //
              && slr.lookahead(curIdx - 3) == '%' //
              ) {
            // check whether we have another %. This is not syntactically
            // correct, but I assume that the reader will accept %EOF too.
            if (curIdx - 5 >= 0 && slr.lookahead(curIdx - 4) == '%')
              return curIdx - 5;
            else
              return curIdx - 4;
          }
        }
      } else if (c == -1) {
        throw new EOFException("premature end of file");
      }
    }
    LOGGER.warn("no %%EOF marker has been found.");
    return -1;
  }

  protected PDFFormatVersionInfo readVersion(SeekableInputStream sis) throws IOException {

    final SeekableLookaheadStream slr = new ConcurrentSeekableLookaheadStream(sis);

    final long currentPosition = slr.getStreamPosition();

    if (currentPosition != 0)
      slr.seek(0);

    int c = 0;
    while ((c = slr.read()) >= 0) {
      if (c == '%' && slr.lookahead(1) == 'P' && slr.lookahead(2) == 'D' && slr.lookahead(3) == 'F') {
        slr.consume(3); // consume the characters looked ahead

        // check if we've got the classical PDF document
        if (slr.lookahead(1) == '-' && slr.lookahead(3) == '.') {
          final int major = slr.lookahead(2) - '0';
          // get the document version
          return new PDFFormatVersionInfo(major, slr.lookahead(4) - '0');
        }
      }
    }
    LOGGER.error("unable to read out PDF document version");
    // no version information found. Return default
    return PDFFormatVersionInfo.UNKNOWN_FORMAT;
  }

  public PDFDocumentStructure parse() throws IOException {

    final PDFFormatVersionInfo ver = readVersion(sis);
    final long length = determineFileLength(sis);
    final long xrefTableLoc = readMainXRefTableLoc(sis, length);

    CrossReferenceParseData data = null;
    IncrementalUpdate root = null;
    IncrementalUpdate cur = null;

    do {

      final PDFDocumentStructureLexer lexer = new PDFDocumentStructureLexer(sis);
      if (data == null) {
        //
        lexer.seek(xrefTableLoc);
      } else {
        lexer.seek(data.getTrailer().getPrev());
      }
      data = parseCrossReferenceTable(lexer);
      final CrossReferenceTable newXRefT = data.getCrossReferenceTable();

      final IncrementalUpdate currentIncUpd = new IncrementalUpdate(newXRefT, data.getTrailer());

      if (root == null) {
        root = currentIncUpd;
      } else {
        cur.setPrevious(currentIncUpd);
        cur.getCrossReferenceTable().setPrev(currentIncUpd.getCrossReferenceTable());
      }
      cur = currentIncUpd;

    } while (data.getTrailer().getPrev() > 0);

    return new PDFDocumentStructure(ver, length, root);
  }

  protected CrossReferenceParseData parseCrossReferenceTable(PDFDocumentStructureLexer lexer) throws IOException {

    Token t = lexer.peekToken(0);

    if (t.isOfType(TokenTypes.TOKEN_TYPE_NUMBER)) {
      // cross reference stream.
      return parseCrossReferenceStream(lexer);
    }

    // classical cross reference table

    final CrossReferenceTable xrefT = new CrossReferenceTable(100);

    t = lexer.getNextToken();
    if (t != PDFDocumentStructureLexer.TOKEN_KEYWORD_XREF)
      throw new RuntimeException("specified location does not contain a cross reference table");

    long currentObjectNumber = 0;
    while (!isXRefTableEnd(lexer)) {
      final long n1 = parseInteger(lexer).longValue();
      final int n2 = parseInteger(lexer).intValue();

      final Token next = lexer.peekToken(0);
      if (!next.isOfType(TokenTypes.TOKEN_TYPE_NUMBER)) {
        // single reference entry. n1 is the offset, n2 the generation number

        if (isUsedKeyword(next)) {
          xrefT.add(currentObjectNumber, n2, new IObjectLocator.Location(n1));
        } else if (!isFreeKeyword(next)) {
          // some special case: there might be either a empty xref table or
          // empty subsection. In such a case the keyword we are facing is trailer

          if (isTrailerKeyword(next)) {
            // found the trailer keyword
            break;
          }

          throw new RuntimeException(
              "cross reference table contained illegal content. Either used or free markers are allowed.");
        }
        lexer.getNextToken();
        currentObjectNumber++;
        // FIXME add support for free objects (masking existing)
      } else {
        // new section n1 is the new object number, n2 the count of elements
        // following
        currentObjectNumber = n1;
      }
    }
    // done with the xref table. Parse the trailer now. Checking for the trailer
    // keyword will be done in the isXRefTableEnd method
    lexer.getNextToken();

    // there should be a dictionary start token following
    assertTokenType(lexer.getNextToken(), TokenTypesPDF.DICTIONARY_BEGIN);

    // process the trailer dictionary
    final PDFParser p = new PDFParser(lexer, null, new NoSecurityHandler(), filterFactory);
    final DSDictionary trailerDict = p.parseDictionary(-1, -1);
    return new CrossReferenceParseData(xrefT, new Trailer(trailerDict));
  }

  protected boolean isTrailerKeyword(Token next) {

    // is it the instance?
    if (next == PDFDocumentStructureLexer.TOKEN_KEYWORD_TRAILER) {
      return true;
    }

    final byte[] stringToken = next.getStringToken();

    final byte[] keywordToken = PDFDocumentStructureLexer.TOKEN_KEYWORD_TRAILER.getStringToken();

    return Arrays.equals(stringToken, keywordToken);
  }

  protected boolean isFreeKeyword(Token t) {
    return t == PDFDocumentStructureLexer.TOKEN_KEYWORD_FREE;
  }

  protected boolean isUsedKeyword(Token t) {
    return t == PDFDocumentStructureLexer.TOKEN_KEYWORD_USED;
  }

  private boolean isXRefTableEnd(PDFDocumentStructureLexer lexer) throws IOException {
    final Token t = lexer.peekToken(0);
    if (t.isOfType(TokenTypes.TOKEN_TYPE_STRING)) {
      if (t != PDFDocumentStructureLexer.TOKEN_KEYWORD_TRAILER)
        throw new RuntimeException("cross reference table is corrupt");
      return true;
    }
    return false;
  }

  protected Number parseInteger(IPDFLexer lexer) throws IOException {
    final Token objectNumberToken = lexer.getNextToken();
    assertTokenType(objectNumberToken, TokenTypesPDF.INT);
    return objectNumberToken.getNumberToken();
  }

  protected CrossReferenceParseData parseCrossReferenceStream(PDFDocumentStructureLexer lexer) throws IOException {

    final PDFParser p = new PDFParser(lexer, null, new NoSecurityHandler(), filterFactory);
    final DSObject o = p.parseObject();
    if (!(o instanceof DSStream)) {
      throw new RuntimeException("expected cross reference stream. Structure is corrupt.");
    }
    final SeekableInputStream xrefStream = filterFactory.getInputStreamFromPDFStream((DSStream) o);

    final CrossReferenceTable xrefT = new CrossReferenceTable(50);

    final CrossReferenceStreamDictionary xrefStreamDict = new CrossReferenceStreamDictionary(
        ((DSStream) o).getDictionary());

    final int[] w = xrefStreamDict.getW();
    final Subsection[] index = xrefStreamDict.getIndex();
    if (w[1] <= 0)
      // the second field is the only one that never has any default
      throw new RuntimeException("incorrect element in W array");

    for (final Subsection subsection : index) {

      final int count = subsection.getCount();
      long currentObjectNumber = subsection.getObjectNumber();

      for (int i = 0; i < count; i++) {
        final long[] segment = readSegment(xrefStream, w);

        if (segment[0] == 0) {
          // there seems to be no use for the nextFreeObject field found at idx
          // 2 in the segment array. Maybe this only points to another existing
          // "free" entry in the cross reference table...
          // long nextFreeobject = segment[1];
          currentObjectNumber++;
        } else if (segment[0] == 1) {
          // non compressed object offset
          final long offset = segment[1];
          final int generationNumber = (int) segment[2];
          xrefT.add(currentObjectNumber++, generationNumber, new Location(offset));
        } else if (segment[0] == 2) {
          final long parentObjectNumber = segment[1];
          final int objectIndex = (int) segment[2];
          xrefT.add(currentObjectNumber++, 0, new Location(parentObjectNumber, objectIndex));

        } else {
          LOGGER.info("Unknown type of entry in cross reference stream: " + segment[0]);
        }

      }
    }

    return new CrossReferenceParseData(xrefT, xrefStreamDict);
  }

  private long[] readSegment(SeekableInputStream xrefStream, int[] w) throws IOException {
    final long[] segment = new long[3];

    // first segment
    if (w[0] == 0) {
      segment[0] = 1;
    } else {
      final byte[] buf = new byte[w[0]];

      if (readFully(xrefStream, buf) < 0)
        // if this exception is thrown, chances are good, that the /Index is wrong. The first value
        // of each pair is the first object number. The second value is the number of entries, not
        // the total number of objects...
        throw new RuntimeException("cross reference stream data corrupted");

      segment[0] = asLong(buf);
    }

    // second segment
    if (w[1] == 0) {
      // well, this would mean that the PDF file consists of only free object
      // markers... How useful!
      if (segment[0] != 0)
        throw new RuntimeException("cross reference stream data corrupted");
    } else {
      final byte[] segment2Buf = new byte[w[1]];

      if (readFully(xrefStream, segment2Buf) < 0)
        throw new RuntimeException("cross reference stream data corrupted");

      segment[1] = asLong(segment2Buf);
    }

    // third segment
    if (w[2] == 0) {
      // if (segment[0] == 0 || segment[0] == 2)
      // throw new RuntimeException(
      // StructureMessages.XREFS_STREAM_CORRUPT);
      // for segment[0] == 1 we have a default of 0

      // other implementations seem to assume 0 as the default
      segment[2] = 0;
    } else {
      final byte[] segment3Buf = new byte[w[2]];

      if (readFully(xrefStream, segment3Buf) < 0)
        throw new RuntimeException("cross reference stream data corrupted");

      segment[2] = asLong(segment3Buf);
    }
    return segment;
  }

  private long asLong(byte[] buf) {
    long result = 0;

    for (final byte b : buf) {
      result = result << 8 | b & 0xff;
    }
    return result;
  }

  private int readFully(SeekableInputStream xrefStream, final byte[] buf) throws IOException {
    int read;
    int pos = 0;
    while ((read = xrefStream.read(buf, pos, buf.length - pos)) != -1 && pos < buf.length) {
      pos += read;
    }
    return pos;
  }
}

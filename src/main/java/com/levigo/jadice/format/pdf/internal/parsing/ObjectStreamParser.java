package com.levigo.jadice.format.pdf.internal.parsing;

import java.io.IOException;

import com.levigo.jadice.format.pdf.internal.PDFFilterFactory;
import com.levigo.jadice.format.pdf.internal.crypt.NoSecurityHandler;
import com.levigo.jadice.format.pdf.internal.msg.StructureMessages;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSNumber;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;
import com.levigo.jadice.format.pdf.internal.objects.DSStream;

public class ObjectStreamParser extends AbstractParserSupport {

  private final IPDFLexer lexer;
  private final PDFParser parser;
  private final int objectCount;
  private final long[][] objectIndexes;
  private final long offsetToFirst;

  public ObjectStreamParser(DSStream objectStream, IObjectLocator locator, PDFFilterFactory filterFactory)
      throws IOException {

    lexer = new PDFLexer(filterFactory.getInputStreamFromPDFStream(objectStream));

    // create a parser with a no-op security handler. Object streams are not allowed to contain
    // encrypted objects as the stream itself might already be encrypted.
    parser = new PDFParser(lexer, locator, new NoSecurityHandler(), filterFactory, true);

    final DSDictionary dict = objectStream.getDictionary();

    objectCount = getNumber(dict, "N", StructureMessages.OBJSTM_N_MISSING).getInteger();
    offsetToFirst = getNumber(dict, "First", StructureMessages.OBJSTM_FIRST_MISSING).getInteger();

    objectIndexes = new long[objectCount][];

    for (int i = 0; i < objectIndexes.length; i++) {
      objectIndexes[i] = new long[]{//
          parser.parseInteger().longValue(), //
          parser.parseInteger().longValue(),
      };
    }
  }

  private DSNumber getNumber(final DSDictionary dict, final String key, final String msg) {
    final DSObject nObj = dict.getNamedEntryValue(key);
    if (nObj == null || !(nObj instanceof DSNumber)) {
      throw new RuntimeException(msg);
    }

    final DSNumber num = (DSNumber) nObj;
    return num;
  }


  public DSObject parse(final long objectNumber, final int objectIndex) throws IOException {
    if (objectIndex > objectCount)
      // FIXME error here or shouldn't we care?
      return null;

    long offset = -1;
    if (objectIndexes[objectIndex][0] != objectNumber) {
      // objectIndex doesn't match the value stored in the index table.
      // Trying to find that object
      for (final long[] objectIndexe : objectIndexes) {
        if (objectIndexe[0] == objectNumber) {
          offset = objectIndexe[1];
          break;
        }
      }
    } else {
      // simple case. Found the object as we've expected
      offset = objectIndexes[objectIndex][1];
    }

    if (offset < 0)
      // FIXME as above, some error here?
      return null;

    lexer.seek(offsetToFirst + offset);
    return parser.parseObjectBody(objectNumber, 0);
  }

}

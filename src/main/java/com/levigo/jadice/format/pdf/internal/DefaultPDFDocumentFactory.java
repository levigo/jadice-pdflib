package com.levigo.jadice.format.pdf.internal;

import java.io.IOException;

import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.pdf.crypt.PDFSecurityException;
import com.levigo.jadice.format.pdf.internal.crypt.NoSecurityHandler;
import com.levigo.jadice.format.pdf.internal.crypt.SecurityHandler;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSReference;
import com.levigo.jadice.format.pdf.internal.parsing.CrossReferenceTable;
import com.levigo.jadice.format.pdf.internal.parsing.PDFDocumentStructureParser;
import com.levigo.jadice.format.pdf.internal.parsing.PDFDocumentStructureParser.IncrementalUpdate;
import com.levigo.jadice.format.pdf.internal.parsing.PDFDocumentStructureParser.PDFDocumentStructure;
import com.levigo.jadice.format.pdf.internal.parsing.PDFLexer;
import com.levigo.jadice.format.pdf.internal.parsing.PDFParser;
import com.levigo.jadice.format.pdf.internal.struct.Trailer;

public class DefaultPDFDocumentFactory implements PDFDocumentFactory {

  @Override
  public PDFDocument create(final SeekableInputStream source) throws IOException, PDFSecurityException {


    // prepare and run document structure parser
    final ReferenceResolver allButReferenceResolver = ref -> {
      if (ref instanceof DSReference) {
        throw new IllegalArgumentException("This resolver only supports direct objects");
      }

      return ref;
    };
    final PDFFilterFactory initFilterFactory = new PDFFilterFactory(allButReferenceResolver);
    final PDFDocumentStructureParser docStructParser = new PDFDocumentStructureParser(source, initFilterFactory);
    final PDFDocumentStructure docStruct = docStructParser.parse();
    final IncrementalUpdate activeUpdate = docStruct.getActiveUpdate();


    // retrieve cross reference table and trailer
    final CrossReferenceTable crossReferenceTable = activeUpdate.getCrossReferenceTable();
    final Trailer trailer = activeUpdate.getTrailer();


    // handle encryption and build security handler
    final PDFLexer lexer = new PDFLexer(source);
    final PDFParser encryptionParser = new PDFParser(lexer, crossReferenceTable, new NoSecurityHandler());
    final ReferenceResolver encryptionResolver = new DefaultReferenceResolver(encryptionParser);

    // FIXME the current version of this extracted library doesn't have the decryption support.
    final SecurityHandler securityHandler = new NoSecurityHandler();
    if (!securityHandler.authenticate()) {
      throw new PDFSecurityException("Access denied. Document will not be displayed.");
    }


    // build and configure objects for final use
    final PDFParser docParser = new PDFParser(lexer, crossReferenceTable, securityHandler);
    final ReferenceResolver docResolver = new DefaultReferenceResolver(docParser);
    final PDFFilterFactory docFilterFactory = new PDFFilterFactory(docResolver, securityHandler);
    final DSDictionary root = docResolver.resolveDictionary(trailer.getRoot());

    // ready to build pdf document
    return new PDFDocument(docResolver, docFilterFactory, securityHandler, trailer, root, docStruct);
  }

}

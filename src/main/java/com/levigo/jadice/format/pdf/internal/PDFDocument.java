package com.levigo.jadice.format.pdf.internal;

import com.levigo.jadice.format.pdf.internal.crypt.SecurityHandler;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.parsing.PDFDocumentStructureParser.PDFDocumentStructure;
import com.levigo.jadice.format.pdf.internal.struct.Trailer;

public class PDFDocument {

  private final ReferenceResolver resolver;
  private final PDFFilterFactory filterFactory;
  private final SecurityHandler securityHandler;
  private final Trailer trailer;
  private final DSDictionary catalog;
  private final PDFDocumentStructure documentStructure;

  public PDFDocument(ReferenceResolver resolver, PDFFilterFactory filterFactory, SecurityHandler securityHandler,
      Trailer trailer, DSDictionary catalog, PDFDocumentStructure documentStructure) {
    this.resolver = resolver;
    this.filterFactory = filterFactory;
    this.securityHandler = securityHandler;
    this.trailer = trailer;
    this.catalog = catalog;
    this.documentStructure = documentStructure;
  }

  public ReferenceResolver getResolver() {
    return resolver;
  }

  public PDFFilterFactory getFilterFactory() {
    return filterFactory;
  }

  public SecurityHandler getSecurityHandler() {
    return securityHandler;
  }

  public Trailer getTrailer() {
    return trailer;
  }

  public DSDictionary getCatalog() {
    return catalog;
  }

  public PDFDocumentStructure getDocumentStructure() {
    return documentStructure;
  }

}

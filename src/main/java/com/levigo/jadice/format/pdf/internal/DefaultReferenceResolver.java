package com.levigo.jadice.format.pdf.internal;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.levigo.jadice.format.pdf.internal.msg.GlobalMessages;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;
import com.levigo.jadice.format.pdf.internal.objects.DSReference;
import com.levigo.jadice.format.pdf.internal.parsing.PDFParser;

public class DefaultReferenceResolver implements ReferenceResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultReferenceResolver.class);

  private final PDFParser parser;

  public DefaultReferenceResolver(PDFParser parser) {
    this.parser = parser;
  }

  @Override
  public DSObject resolve(DSObject obj) {
    if (obj instanceof DSReference) {
      try {
        final DSReference ref = (DSReference) obj;
        synchronized (parser) {
          obj = parser.parseObject(ref.getReferencedObjectNumber(), ref.getReferencedGenerationNumber());
        }
      } catch (final IOException e) {
        LOGGER.error(GlobalMessages.IOEXCEPTION_OBJECT_RESOLVING, e);
        return null;
      }
    }

    return obj;
  }

}

package com.levigo.jadice.format.pdf.internal.crypt;

import java.util.Collection;

/**
 * a set of permissions as defined by PDF Security Handlers.
 *
 * @param <P> the type of {@link PDFPermission}s this set of permissions holds.
 */
public interface PDFPermissions<P extends PDFPermission> {

  Collection<P> getAllowed();

  Collection<P> getDenied();
}

package com.levigo.jadice.format.pdf.internal.objects;

/**
 * DSNullObject is a dummy object, containing no data. As the PDF-specification prescibes references
 * to non existend Objects are the PDFNullObjects with no data, this type is passed back if a non
 * existend object was referenced.
 */
public class DSNullObject extends DSObject {

  public static final DSNullObject INSTANCE = new DSNullObject();

  private DSNullObject() {
  }

  /**
   * @see com.levigo.jadice.format.pdf.internal.objects.DSObject#equals(com.levigo.jadice.format.pdf.internal.objects.DSObject)
   */
  @Override
  public boolean equals(DSObject object) {
    return object instanceof DSNullObject;
  }

  @Override
  public String toString() {
    return "null";
  }
}

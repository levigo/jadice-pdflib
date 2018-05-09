package com.levigo.jadice.format.pdf.internal.objects;

public abstract class DSNumber extends DSObject {

  /**
   * @see com.levigo.jadice.format.pdf.internal.objects.DSObject#equals(com.levigo.jadice.format.pdf.internal.objects.DSObject)
   */
  @Override
  public abstract boolean equals(DSObject object);

  /**
   * this method will return the by the <code>PDFNumber</code> derived class's value as a
   * <code>double</code>. This could cause data loss. <br>
   * any <code>PDFNumber</code> derived type has to implement this method.
   *
   * @return the object value as <code>double</code>
   */
  public abstract double getDouble();

  /**
   * this method will return the by the <code>PDFNumber</code> derived class's value as a
   * <code>float</code>. This could cause data loss. <br>
   * any <code>PDFNumber</code> derived type has to implement this method.
   *
   * @return the object value as <code>float</code>
   */
  public abstract float getFloat();

  /**
   * this method will return the by the <code>PDFNumber</code> derived class's value as a
   * <code>int</code>. This could cause data loss. <br>
   * any <code>PDFNumber</code> derived type has to implement this method.
   *
   * @return the object value as <code>int</code>
   */
  public abstract int getInteger();

  /**
   * this method will return the by the <code>PDFNumber</code> derived class's value as a
   * <code>long</code>. This could cause data loss. <br>
   * any <code>PDFNumber</code> derived type has to implement this method.
   *
   * @return the object value as <code>long</code>
   */
  public abstract long getLong();

  /**
   * overrides {@link DSObject#isNumber()} and will return true for all <code>PDFNumber</code>
   * derived classes
   */
  @Override
  public final boolean isNumber() {
    return true;
  }
}

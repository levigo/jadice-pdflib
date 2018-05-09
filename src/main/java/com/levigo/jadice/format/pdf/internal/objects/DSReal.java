package com.levigo.jadice.format.pdf.internal.objects;

public final class DSReal extends DSNumber {

  private final double value;

  /**
   * @param value
   */
  public DSReal(double value) {
    this.value = value;
  }

  /**
   * @return the value of the <code>PDFReal</code>
   */
  public double getValue() {
    return value;
  }

  /**
   * checks for value equality. If the passed object is not of type {@link DSNumber},
   * <code>false</code> will be returned
   *
   * @param object the object to compare with
   * @return true if equal, otherwise false
   */
  @Override
  public boolean equals(DSObject object) {
    if (object instanceof DSNumber)
      return ((DSNumber) object).getDouble() == value;
    return false;
  }

  /**
   * returns a {@link String}of the format:
   *
   * <pre>
   *  &lt;Value&gt; (Real)
   * </pre>
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getValue() + " (Real)";
  }

  /**
   * @see com.levigo.jadice.format.pdf.internal.objects.DSNumber#getDouble()
   */
  @Override
  public double getDouble() {
    return getValue();
  }

  /**
   * @see com.levigo.jadice.format.pdf.internal.objects.DSNumber#getInteger()
   */
  @Override
  public int getInteger() {
    return (int) getValue();
  }

  /**
   * @see com.levigo.jadice.format.pdf.internal.objects.DSNumber#getLong()
   */
  @Override
  public long getLong() {
    return (long) getValue();
  }

  @Override
  public float getFloat() {
    return (float) getValue();
  }
}

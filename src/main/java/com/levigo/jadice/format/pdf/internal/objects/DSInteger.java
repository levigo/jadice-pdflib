package com.levigo.jadice.format.pdf.internal.objects;


public final class DSInteger extends DSNumber implements Comparable<DSInteger> {

  private final long value;

  public DSInteger(long value) {
    this.value = value;
  }

  public long getValue() {
    return value;
  }

  /**
   * checks for value equality. If the passed object is not {@link DSInteger}, <code>false</code>
   * will be returned.
   *
   * @param object the object to compare with
   * @return true if equal, <code>false</code> otherwise
   */
  @Override
  public boolean equals(DSObject object) {
    if (object instanceof DSInteger) {
      if (((DSInteger) object).getValue() == value)
        return true;
    }
    return false;
  }

  /**
   * returns a {@link String} of the format:
   *
   * <pre>
   * &lt;Value&gt; (Integer)
   * </pre>
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getValue() + " (Integer)";
  }

  @Override
  public double getDouble() {
    return getValue();
  }

  @Override
  public int getInteger() {
    return (int) getValue();
  }

  @Override
  public long getLong() {
    return getValue();
  }

  @Override
  public float getFloat() {
    return getValue();
  }

  @Override
  public int compareTo(DSInteger o) {
    return value < o.value ? -1 : value > o.value ? 1 : 0;
  }
}

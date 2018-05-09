package com.levigo.jadice.format.pdf.internal.objects;

/**
 * PDF representation of a boolean value.
 */
public class DSBoolean extends DSObject {

  /**
   * this constant may be used for direct objects
   */
  public static final DSBoolean TRUE = new DSBoolean(true);

  /**
   * this constant may be used for direct objects
   */
  public static final DSBoolean FALSE = new DSBoolean(false);

  private final boolean value;

  /**
   * creates a new instance, representing the given boolean value. In cases where the
   * {@link DSBoolean} is to be used as an indirect object, an individual instance must be created
   * to prevent object-/generation-number collisions.
   *
   * @param value
   */
  public DSBoolean(boolean value) {
    this.value = value;
  }

  /**
   * returns the booelan value represented by this instance
   *
   * @return either true or false
   */
  public boolean getValue() {
    return this.value;
  }

  @Override
  public boolean equals(DSObject object) {
    return object instanceof DSBoolean && ((DSBoolean) object).value == value;
  }

  /**
   * This method is mainly used for the PDF-DocInspector
   */
  @Override
  public String toString() {
    return getValue() + " (Boolean)";
  }
}

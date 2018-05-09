package com.levigo.jadice.format.pdf.internal.objects;


public abstract class DSObject {

  // default values are negative to identify objects which are not numbered
  private long thisObjectNumber = -1;
  private int thisGenerationNumber = -1;

  /**
   * checks for value equality.
   *
   * @param object the object to compare with
   * @return <code>true</code> if equal, otherwise <code>false</code>
   */
  public abstract boolean equals(DSObject object);

  @Override
  public final boolean equals(Object obj) {
    if (obj instanceof DSObject)
      return equals((DSObject) obj);
    return false;
  }

  /**
   * returns <code>true</code> if the object is representing an object of type {@link DSNumber}
   *
   * @return <code>true</code> if {@link DSNumber} object
   */
  public boolean isNumber() {
    return false;
  }

  /**
   * returns <code>true</code> if the object is representing an object of type {@link DSArray}
   *
   * @return <code>true</code> if {@link DSArray} object
   */
  public boolean isArray() {
    return false;
  }

  /**
   * returns <code>true</code> if the object is representing an object of type {@link DSDictionary}
   *
   * @return <code>true</code> if {@link DSDictionary} object
   */
  public boolean isDictionary() {
    return false;
  }

  /**
   * returns <code>true</code> if the object is representing an object of type {@link DSNameObject}
   *
   * @return <code>true</code> if {@link DSNameObject} object
   */
  public boolean isNameObject() {
    return false;
  }

  public boolean isString() {
    return false;
  }

  /**
   * returns the generation number of this object. Keep in mind that this method differs from
   * {@link DSReference#getReferencedGenerationNumber()}, which will give the generation number the
   * reference points to
   */
  public final int generationNumber() {
    return thisGenerationNumber;
  }

  /**
   * sets the generation number of this object
   */
  public final void setThisObjectsGenerationNumber(int generationNumber) {
    thisGenerationNumber = generationNumber;
  }

  /**
   * returns the object number of this object. Keep in mind that this method differs from
   * {@link DSReference#getReferencedObjectNumber()}, which will give the object number the
   * reference points to
   */
  public final long objectNumber() {
    return thisObjectNumber;
  }

  /**
   * sets the object number of this object
   */
  public final void setThisObjectsObjectNumber(long objectNumber) {
    thisObjectNumber = objectNumber;
  }
}

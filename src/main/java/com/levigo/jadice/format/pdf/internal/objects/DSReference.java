package com.levigo.jadice.format.pdf.internal.objects;

public class DSReference extends DSObject implements Comparable<DSReference> {

  private final long objectNumber;
  private final int generationNumber;

  /**
   * @param objectNumber     the object number of the reference
   * @param generationNumber the generation number of the reference
   */
  public DSReference(long objectNumber, int generationNumber) {
    this.objectNumber = objectNumber;
    this.generationNumber = generationNumber;
  }

  /**
   * @return the generation number of the {@link DSReference} object
   */
  public int getReferencedGenerationNumber() {
    return generationNumber;
  }

  /**
   * @return the object number of the {@link DSReference} object
   */
  public long getReferencedObjectNumber() {
    return objectNumber;
  }

  public boolean isValid() {
    return objectNumber > 0 && generationNumber >= 0;
  }

  /**
   * checks for value equality. If the passed object is not of the <code>PDFReference</code> false
   * will be returned
   *
   * @param object the object to compare with
   * @return true if equal otherwise false
   */
  @Override
  public boolean equals(DSObject object) {
    if (object instanceof DSReference)
      if (compareTo((DSReference) object) == 0) {
        return true;
      }
    return false;
  }

  /**
   * This method is mainly used for the PDF-DocInspector
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getReferencedObjectNumber() + " " + getReferencedGenerationNumber() + " R";
  }

  @Override
  public int compareTo(DSReference ref) {

    // check if the two instances are value equal
    if (getReferencedObjectNumber() == ref.getReferencedObjectNumber()
        && getReferencedGenerationNumber() == ref.getReferencedGenerationNumber()) {
      // the two instances are equal. return 0
      return 0;
    } else if (getReferencedObjectNumber() > ref.getReferencedObjectNumber()) {
      // the local object number is higher
      return 1;
    } else if (getReferencedObjectNumber() == ref.getReferencedObjectNumber()
        && getReferencedGenerationNumber() > ref.getReferencedGenerationNumber()) {
      // the generation number of the local object is higher
      return 1;
    } else {
      // passed object is higher
      return -1;
    }
  }

  @Override
  public int hashCode() {
    return (int) objectNumber;
  }


}

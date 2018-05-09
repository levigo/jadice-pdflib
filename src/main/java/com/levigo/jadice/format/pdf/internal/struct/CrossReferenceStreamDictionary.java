package com.levigo.jadice.format.pdf.internal.struct;

import com.levigo.jadice.format.pdf.internal.msg.StructureMessages;
import com.levigo.jadice.format.pdf.internal.objects.DSArray;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSInteger;
import com.levigo.jadice.format.pdf.internal.objects.DSNumber;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;

public class CrossReferenceStreamDictionary extends Trailer {
  public static final class Subsection implements Comparable<Subsection> {
    private final long objectNumber;
    private final int count;

    public Subsection(long objectNumber, int count) {
      super();
      this.objectNumber = objectNumber;
      this.count = count;
    }

    public int getCount() {
      return count;
    }

    public long getObjectNumber() {
      return objectNumber;
    }

    @Override
    public int hashCode() {
      return (int) (objectNumber << 5 ^ count);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Subsection))
        return false;
      Subsection o = (Subsection) obj;
      return compareTo(o) == 0 && count == o.count;
    }

    public int compareTo(Subsection o) {
      // we're comparing only the objectNumber
      return objectNumber < o.objectNumber ? -1 : (objectNumber == o.objectNumber ? 0 : 1);
    }

    @Override
    public String toString() {
      return getClass().getName() + "[" + objectNumber + "," + count + "]";
    }
  }

  public CrossReferenceStreamDictionary(DSDictionary xrefStreamDict) {
    super(xrefStreamDict);
  }

  public Subsection[] getIndex() {
    final DSObject indexObject = getDictionary().getNamedEntryValue("Index");
    if (indexObject != null) {

      if (!(indexObject instanceof DSArray))
        throw new RuntimeException(StructureMessages.XREFS_INDEX_ARRAY_ERROR);

      final DSArray indexArray = ((DSArray) indexObject);
      if (indexArray.size() % 2 != 0)
        throw new RuntimeException(StructureMessages.XREFS_INDEX_ARRAY_ERROR);

      final int pairs = indexArray.size() / 2;

      Subsection[] subsections = new Subsection[pairs];

      for (int i = 0; i < subsections.length; i++) {

        final DSObject startObject = indexArray.get(i * 2);
        final DSObject count = indexArray.get((i * 2) + 1);

        if (startObject == null || !(startObject instanceof DSNumber) || count == null || !(count instanceof DSNumber))
          throw new RuntimeException(StructureMessages.XREFS_INDEX_ARRAY_ERROR);

        subsections[i] = new Subsection( //
            ((DSNumber) startObject).getLong(), //
            ((DSNumber) count).getInteger() //
        );
      }
      return subsections;
    }

    // use the size entry
    return new Subsection[]{new Subsection(0L, getSize())
    };
  }

  // overriding getSize() as the size is really required for cross reference
  // stream dictionaries. In case of simple cross reference tables we are able
  // to live without the size
  @Override
  public int getSize() {
    final DSObject sizeObject = getDictionary().getNamedEntryValue("Size");
    if (sizeObject == null)
      throw new RuntimeException(StructureMessages.XREFS_SIZE_MISSING);

    if (!(sizeObject instanceof DSNumber))
      throw new RuntimeException(StructureMessages.XREFS_SIZE_ERROR);

    return ((DSNumber) sizeObject).getInteger();
  }

  public int[] getW() {
    final DSObject e = getDictionary().getNamedEntryValue("W");
    if (!(e instanceof DSArray))
      throw new RuntimeException(StructureMessages.XREFS_MISSING_W_ARRAY);
    DSArray wObj = (DSArray) e;
    if (wObj.size() != 3)
      throw new RuntimeException(
          "the size of the W array in the cross reference stream has incorrect length. Expected: 3 Actual: "
              + wObj.size());

    final int[] w = new int[]{requireInt(wObj.get(0), StructureMessages.XREFS_W_INCORRECT_CONTENT),
        requireInt(wObj.get(1), StructureMessages.XREFS_W_INCORRECT_CONTENT),
        requireInt(wObj.get(2), StructureMessages.XREFS_W_INCORRECT_CONTENT)
    };
    return w;
  }

  private int requireInt(DSObject o, String msg) {
    if (!(o instanceof DSInteger))
      throw new RuntimeException(msg);
    return ((DSInteger) o).getInteger();
  }
}

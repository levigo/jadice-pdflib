package com.levigo.jadice.format.pdf.internal.parsing;

import com.levigo.jadice.format.pdf.internal.objects.DSReference;


public interface IObjectLocator {
  public static final class Location {
    private final boolean nested;
    private final long offset;
    private final int objectIndex;
    private final long surroundingObjectNumber;


    /**
     * generates a nested object reference.
     *
     * @param offset
     * @param objectIndex
     */
    public Location(long surroundingObjectNumber, int objectIndex) {
      super();
      this.surroundingObjectNumber = surroundingObjectNumber;
      nested = true;
      this.offset = -1;
      this.objectIndex = objectIndex;
    }

    /**
     * generates a indirect top level object reference.
     *
     * @param offset
     */
    public Location(long offset) {
      super();
      nested = false;
      this.offset = offset;
      surroundingObjectNumber = -1;
      objectIndex = -1;
    }

    public boolean isNested() {
      return nested;
    }

    public long getSurroundingObjectNumber() {
      return surroundingObjectNumber;
    }

    public int getObjectIndex() {
      return objectIndex;
    }

    public long getOffset() {
      return offset;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (nested ? 1231 : 1237);
      result = prime * result + objectIndex;
      result = prime * result + (int) (offset ^ (offset >>> 32));
      result = prime * result + (int) (surroundingObjectNumber ^ (surroundingObjectNumber >>> 32));
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Location))
        return false;
      Location other = (Location) obj;
      return nested == other.nested && objectIndex == other.objectIndex && offset == other.offset
          && surroundingObjectNumber == other.surroundingObjectNumber;
    }
  }

  Location locate(DSReference ref);

  Location locate(long objectNumber, int generationNumber);
}

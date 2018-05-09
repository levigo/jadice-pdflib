package com.levigo.jadice.format.pdf.internal.parsing;

import static com.levigo.jadice.format.pdf.internal.objects.DS.ref;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.levigo.jadice.format.pdf.internal.objects.DSReference;

public class CrossReferenceTable implements IObjectLocator {

  private final Map<DSReference, Location> locations;
  private CrossReferenceTable prev;

  public CrossReferenceTable(int initialSize) {
    locations = new HashMap<>(initialSize + 5);
  }

  public CrossReferenceTable getPrev() {
    return prev;
  }

  public void setPrev(CrossReferenceTable crt) {
    prev = crt;
  }

  @Override
  public Location locate(DSReference ref) {
    final Location res = locations.get(ref);

    if (res == null && prev != null) {
      return prev.locate(ref);
    }
    return res;

  }

  @Override
  public Location locate(long objectNumber, int generationNumber) {
    return locate(ref(objectNumber, generationNumber));
  }

  public void add(long objectNumber, int generationNumber, Location location) {
    locations.put(ref(objectNumber, generationNumber), location);
  }

  public Set<DSReference> getRegistered() {
    return Collections.unmodifiableSet(locations.keySet());
  }
}

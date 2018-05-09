package com.levigo.jadice.format.pdf.internal.objects;

import static com.levigo.jadice.format.pdf.internal.objects.DS.name;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class DSCommonDictionary extends DSDictionary {

  private final Map<DSNameObject, DSObject> entries;

  public DSCommonDictionary() {
    this(8);
  }

  public DSCommonDictionary(int initialCapacity) {
    entries = new HashMap<>(initialCapacity);
  }

  @Override
  public void addNamedEntry(DSNameObject nameObject, DSObject object) {
    entries.put(nameObject, object);
  }

  @Override
  public DSObject getNamedEntryValue(DSNameObject nameObject) {
    return entries.get(nameObject);
  }

  @Override
  public DSObject getNamedEntryValue(String nameString) {
    // FIXME: können wir uns da das Wrappen nicht irgendwie sparen?
    return getNamedEntryValue(name(nameString));
  }

  @Override
  public Iterator<Map.Entry<DSNameObject, DSObject>> iterator() {
    return entries.entrySet().iterator();
  }

  /**
   * if it's not the same instance, this method will always return <code>false</code> as
   * Dictionaries can't be compared
   *
   * @see com.levigo.jadice.format.pdf.internal.objects.DSObject#equals(com.levigo.jadice.format.pdf.internal.objects.DSObject)
   */
  @Override
  public boolean equals(DSObject object) {
    return this == object;
  }

  @Override
  public int size() {
    return entries.size();
  }

  @Override
  public void removeNamedEntry(DSNameObject name) {
    entries.remove(name);
  }
}

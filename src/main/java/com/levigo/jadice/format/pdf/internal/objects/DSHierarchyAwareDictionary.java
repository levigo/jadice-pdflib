package com.levigo.jadice.format.pdf.internal.objects;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;


public class DSHierarchyAwareDictionary extends DSDictionary {
  private final DSDictionary sourceDict;
  private final DSDictionary parent;
  private List<DSNameObject> deletedList;


  public DSHierarchyAwareDictionary(DSDictionary sourceDict, DSDictionary parent) {
    this.sourceDict = sourceDict;
    this.parent = parent;

    setThisObjectsObjectNumber(sourceDict.objectNumber());
    setThisObjectsGenerationNumber(sourceDict.generationNumber());
  }

  @Override
  public void addNamedEntry(DSNameObject nameObject, DSObject Object) {
    sourceDict.addNamedEntry(nameObject, Object);
  }

  @Override
  public boolean equals(DSObject object) {
    return sourceDict.equals(object);
  }

  @Override
  public DSObject getNamedEntryValue(DSNameObject nameObject) {

    if (deletedList != null && deletedList.indexOf(nameObject) >= 0) {
      return null;
    }

    DSObject result = sourceDict.getNamedEntryValue(nameObject);

    if (result == null) {
      result = parent.getNamedEntryValue(nameObject);
    }

    return result;
  }

  @Override
  public DSObject getNamedEntryValue(String nameObject) {

    if (deletedList != null && deletedList.indexOf(new DSNameObject(nameObject)) >= 0) {
      return null;
    }

    DSObject result = sourceDict.getNamedEntryValue(nameObject);

    if (result == null) {
      result = parent.getNamedEntryValue(nameObject);
    }

    return result;
  }


  @Override
  public Iterator<Entry<DSNameObject, DSObject>> iterator() {
    // FIXME this should also contain the values that havn't been overridden
    // from the parent
    return sourceDict.iterator();
  }

  @Override
  public void removeNamedEntry(DSNameObject name) {

    // XXX this is slow and should be optimized
    final DSObject entry = sourceDict.getNamedEntryValue(name);

    if (entry != null) {
      sourceDict.removeNamedEntry(name);
    } else {

      if (deletedList == null) {
        deletedList = new Vector<>();
      }

      deletedList.add(name);
    }
  }

  @Override
  public int size() {
    // FIXME das ist nicht ganz korrekt. Durch die Vererbung koennen es mehr eintraege sein.
    return sourceDict.size();
  }

}

package com.levigo.jadice.format.pdf.internal.objects;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public abstract class DSDictionary extends DSObject implements Iterable<Entry<DSNameObject, DSObject>> {

  /**
   * add an {@link DSNameObject} and {@link DSObject} pair to the dictionary. If there is a
   * {@link DSNameObject} containing <code>/Type</code> the value will be saved to provide faster
   * access to the type.
   *
   * @param NameObject
   * @param Object
   * @see DSDictionary#getType()
   * @see DSDictionary#addNamedEntry(DSNameObject, DSObject)
   */
  public abstract void addNamedEntry(DSNameObject NameObject, DSObject Object);

  /**
   * <code>getNamedEntryValue(PDFNameObject)</code> returnes an PDFObject of the PDFDictionary
   * identified by an PDFNameObject. If the no maching value was found <code>null</code> will be
   * returned.
   *
   * @param nameObject an {@link DSNameObject} which identifies the entry
   * @return the {@link DSObject} found otherwise <code>null</code>
   */
  public abstract DSObject getNamedEntryValue(DSNameObject nameObject);

  /**
   * frontend to {@link #getNamedEntryValue(DSNameObject)} with the difference that the it accepts
   * an <code>String</code> as the identifier
   *
   * @param nameObject an <code>String</code> which identifies the entry
   * @return the {@link DSObject} found otherwise <code>null</code>
   * @see #getNamedEntryValue(DSNameObject)
   */
  public abstract DSObject getNamedEntryValue(String nameObject);

  /**
   * The iterator returned by <code>iterator()</code> of this class is <i>fail-fast </i>: if the
   * Dictionary is structurally modified at any time after the iterator was created, in any way
   * except through the iterator's own <tt>remove</tt> or <tt>add</tt> methods, the iterator throws
   * a <tt>ConcurrentModificationException</tt>. Thus, in the face of concurrent modification, the
   * iterator fails quickly and cleanly, rather than risking arbitrary, non-deterministic behavior
   * at an undetermined time in the future.
   *
   * @return an {@link java.util.Iterator}
   * @throws java.util.ConcurrentModificationException
   */
  @Override
  public abstract Iterator<Map.Entry<DSNameObject, DSObject>> iterator();

  /**
   * @return <code>true</code> as this is a Dictionary
   */
  @Override
  public final boolean isDictionary() {
    return true;
  }

  public abstract void removeNamedEntry(DSNameObject name);

  public abstract int size();
}

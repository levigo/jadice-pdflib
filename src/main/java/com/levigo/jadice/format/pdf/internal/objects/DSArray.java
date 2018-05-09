package com.levigo.jadice.format.pdf.internal.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * the <code>PDFArray</code> is one of the basic PDF types. It can carry any type of PDFObject<br>
 */
public class DSArray extends DSObject implements List<DSObject> {

  /**
   * the list of Objects
   */
  private final List<DSObject> objects;

  /**
   * Contructor <br>
   */
  public DSArray() {
    this(new ArrayList<DSObject>());
  }

  public DSArray(List<DSObject> objects) {
    this.objects = objects;
  }

  public DSArray(DSObject... objects) {
    this.objects = Arrays.asList(objects);
  }

  /**
   * <code>getObject()</code> will return an object of the array identified by the index
   * <code>i</code>
   *
   * @param i the position of the object to get.
   * @return the object on the basis of the parameter <code>i</code>
   */
  @Override
  public DSObject get(int i) {
    return objects.get(i);
  }


  /**
   * The iterators returned by <code>PDFArray.iterator()</code> is <i>fail-fast </i>: if the table
   * is structurally modified at any time after the iterator is created, in any way except through
   * the iterator's own <tt>remove</tt> or <tt>add</tt> methods, the iterator throws a
   * <tt>ConcurrentModificationException</tt>. Thus, in the face of concurrent modification, the
   * iterator fails quickly and cleanly, rather than risking arbitrary, non-deterministic behavior
   * at an undetermined time in the future.
   *
   * @return iterator for the objects in the array
   * @throws java.util.ConcurrentModificationException
   * @see java.util.Iterator
   */
  @Override
  public Iterator<DSObject> iterator() {
    return objects.iterator();
  }

  /**
   * check if the array has any elements in it.
   *
   * @return true if empty otherwise false
   */
  @Override
  public boolean isEmpty() {
    return objects.isEmpty();
  }

  /**
   * check if the passed object is in the array.
   *
   * @param element the object to be checked for
   * @return true if the objects is in the array otherwise false
   */

  /**
   * returns the size of the Array
   *
   * @return the count of objects in the Array
   */
  @Override
  public int size() {
    return objects.size();
  }

  /**
   * adds an object to the array.
   *
   * @param obj the object to be added
   */
  @Override
  public boolean add(DSObject obj) {
    return objects.add(obj);
  }

  @Override
  public void add(int index, DSObject element) {
    objects.add(index, element);
  }

  @Override
  public boolean addAll(Collection<? extends DSObject> c) {
    return objects.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends DSObject> c) {
    return objects.addAll(index, c);
  }

  @Override
  public void clear() {
    objects.clear();
  }

  @Override
  public boolean contains(Object o) {
    return objects.contains(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return objects.containsAll(c);
  }

  @Override
  public int indexOf(Object o) {
    return objects.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return objects.lastIndexOf(o);
  }

  @Override
  public ListIterator<DSObject> listIterator() {
    return objects.listIterator();
  }

  @Override
  public ListIterator<DSObject> listIterator(int index) {
    return objects.listIterator(index);
  }

  @Override
  public DSObject remove(int index) {
    return objects.remove(index);
  }

  @Override
  public boolean remove(Object o) {
    return objects.remove(o);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return objects.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return objects.retainAll(c);
  }

  @Override
  public DSObject set(int index, DSObject element) {
    return objects.set(index, element);
  }

  @Override
  public List<DSObject> subList(int fromIndex, int toIndex) {
    return objects.subList(fromIndex, toIndex);
  }

  @Override
  public Object[] toArray() {
    return objects.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return objects.toArray(a);
  }


  /**
   * @see com.levigo.jadice.format.pdf.internal.objects.DSObject#equals(com.levigo.jadice.format.pdf.internal.objects.DSObject)
   */
  @Override
  public boolean equals(DSObject object) {
    return false;
  }

  /**
   * This method is mainly used for the PDF-DocInspector
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.getClass().getName() + " (# " + size() + ")";
  }

  /**
   * @return a string representation of this {@link DSArray} Object, including a listing of all
   * contents produced by calling <code>toString()</code> on all array elements.
   */
  public String toStringDeep() {
    final StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
    sb.append("(#");
    sb.append(size());
    sb.append("): ");
    for (final Iterator<DSObject> it = iterator(); it.hasNext(); ) {
      final DSObject current = it.next();
      sb.append(current.toString());
      if (it.hasNext())
        sb.append(", ");
    }
    return sb.toString();
  }

  /**
   * overrides {@link DSObject#isArray()} and will return true for all <code>PDFArray</code> derived
   * classes
   */
  @Override
  public final boolean isArray() {
    return true;
  }

}

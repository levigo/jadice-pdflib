package com.levigo.jadice.format.pdf.internal;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayQueue<T> extends AbstractQueue<T> {

  protected class ArrayQueueIterator implements Iterator<T> {
    private final int creationModCount;
    private int element;

    public ArrayQueueIterator(int creationModCount) {
      super();
      this.creationModCount = creationModCount;
    }

    public boolean hasNext() {
      checkModCount();
      return element < size;
    }

    private void checkModCount() {
      if (modCount != creationModCount) {
        throw new ConcurrentModificationException();
      }
    }

    @SuppressWarnings("unchecked")
    public T next() {
      checkModCount();
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return (T) queueElements[getElementIndex(element++)];
    }

    public void remove() {
      throw new UnsupportedOperationException("element removal is not supported");
    }

  }

  protected int modCount;
  protected Object[] queueElements;
  protected int indexToFirst;
  protected int size;

  public ArrayQueue(int initialCapacity) {
    if (initialCapacity <= 0) {
      throw new IllegalArgumentException("illegal initial capacity size: " + initialCapacity);
    }
    queueElements = new Object[initialCapacity];
  }

  /**
   * creates a <b>read only iterator</b>. Removing elements using {@link Iterator#remove()} is not
   * supported an will result in a {@link UnsupportedOperationException}
   */
  @Override
  public Iterator<T> iterator() {
    return new ArrayQueueIterator(modCount);
  }

  @Override
  public int size() {
    return size;
  }

  public boolean offer(T o) {
    modCount++;
    if (size + 1 > queueElements.length) {
      // we need to resize
      final Object[] tmp = new Object[queueElements.length + 10];

      final int headPartLength = queueElements.length - indexToFirst;
      System.arraycopy(queueElements, indexToFirst, tmp, 0, headPartLength);

      if (queueElements.length <= indexToFirst + size) {
        System.arraycopy(queueElements, 0, tmp, headPartLength, getElementIndex(size));
      }

      indexToFirst = 0;
      queueElements = tmp;
    }
    queueElements[getElementIndex(size++)] = o;
    return true;
  }

  public T peek() {
    return peek(0);
  }

  @SuppressWarnings("unchecked")
  public T peek(int idx) {
    if (idx < size) {
      return (T) queueElements[getElementIndex(idx)];
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public T poll() {
    if (size <= 0) {
      return null;
    }
    modCount++;

    final Object elem = queueElements[indexToFirst];
    queueElements[indexToFirst] = null;
    indexToFirst++;
    --size;
    if (indexToFirst >= queueElements.length)
      indexToFirst = 0;
    return (T) elem;
  }

  protected int getElementIndex(int elementNumber) {
    final int off = (indexToFirst + elementNumber);
    if (off >= queueElements.length)
      return off - queueElements.length;
    return off;
  }

  @Override
  public void clear() {
    modCount++;
    // clear all
    // FIXME would it be faster if we only clear those which have been set?
    for (int i = 0; i < queueElements.length; i++) {
      queueElements[i] = null;
    }
    indexToFirst = 0;
    size = 0;
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }
}

package com.levigo.jadice.format.pdf.internal.struct;

import com.levigo.jadice.format.pdf.internal.objects.DSObject;

public abstract class AbstractStructureObject<T extends DSObject> {
  protected final T baseObject;

  protected AbstractStructureObject(T baseObject) {
    super();
    this.baseObject = baseObject;
  }

  public T getBaseObject() {
    return baseObject;
  }
}

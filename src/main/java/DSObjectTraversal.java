import com.levigo.jadice.format.pdf.internal.ReferenceResolver;
import com.levigo.jadice.format.pdf.internal.objects.DSArray;
import com.levigo.jadice.format.pdf.internal.objects.DSBoolean;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSHexString;
import com.levigo.jadice.format.pdf.internal.objects.DSInteger;
import com.levigo.jadice.format.pdf.internal.objects.DSLiteralString;
import com.levigo.jadice.format.pdf.internal.objects.DSNameObject;
import com.levigo.jadice.format.pdf.internal.objects.DSNullObject;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;
import com.levigo.jadice.format.pdf.internal.objects.DSReal;
import com.levigo.jadice.format.pdf.internal.objects.DSReference;
import com.levigo.jadice.format.pdf.internal.objects.DSStream;

public abstract class DSObjectTraversal {
  private final ReferenceResolver resolver;
  private int resolvingDepth;

  public DSObjectTraversal(final ReferenceResolver resolver) {
    this.resolver = resolver;
  }

  public void traverse(DSObject object) {

    if (object instanceof DSDictionary) {
      final DSDictionary dict = (DSDictionary) object;
      visitEnter(dict);

      dict.forEach(e -> {
        visitEntry(e.getKey(), e.getValue());
        traverse(e.getValue());
      });

      visitLeave(dict);
    } else if (object instanceof DSStream) {
      visitEnter((DSStream) object);
      traverse(((DSStream) object).getDictionary());
      visitLeave((DSStream) object);
    } else if (object instanceof DSArray) {
      final DSArray array = (DSArray) object;
      visitEnter(array);

      for (int i = 0; i < array.size(); i++) {
        final DSObject item = array.get(i);
        visitItem(i, item);
        traverse(item);
      }

      visitLeave(array);
    } else if (object instanceof DSReference) {
      final boolean resolveAndTraverse = visitReference((DSReference) object, resolvingDepth);
      if (resolveAndTraverse) {
        resolvingDepth++;
        traverse(resolver.resolve(object));
        resolvingDepth--;
      }
    } else if (object instanceof DSBoolean) {
      visit((DSBoolean) object);
    } else if (object instanceof DSInteger) {
      visit((DSInteger) object);
    } else if (object instanceof DSReal) {
      visit((DSReal) object);
    } else if (object instanceof DSNullObject) {
      visit((DSNullObject) object);
    } else if (object instanceof DSLiteralString) {
      visit((DSLiteralString) object);
    } else if (object instanceof DSHexString) {
      visit((DSHexString) object);
    } else if (object instanceof DSNameObject) {
      visit((DSNameObject) object);
    }
  }

  public void visitEnter(final DSStream object) {
  }

  public void visitLeave(final DSStream object) {
  }

  public void visitItem(int index, final DSObject item) {

  }

  public void visitLeave(final DSArray array) {

  }

  public void visitEnter(final DSArray array) {

  }

  public void visitEntry(final DSNameObject key, final DSObject value) {

  }

  public void visitEnter(DSDictionary dictionary) {

  }

  public void visitLeave(DSDictionary dictionary) {

  }

  /**
   * @param reference
   * @param depth     the number of {@link DSReference references} resolved to get to this object
   * @return {@link true} if the reference shall be resolved and the object the reference is pointing to, shall be traversed.
   */
  public boolean visitReference(DSReference reference, final int depth) {
    return false;
  }

  public void visit(DSBoolean object) {

  }

  public void visit(DSInteger object) {

  }

  public void visit(DSReal object) {

  }

  public void visit(DSNullObject object) {

  }

  public void visit(DSNameObject object) {

  }

  public void visit(DSLiteralString object) {

  }

  public void visit(DSHexString object) {

  }

}

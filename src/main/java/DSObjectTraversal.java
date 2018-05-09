import com.levigo.jadice.format.pdf.internal.ReferenceResolver;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSNameObject;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;

public abstract class DSObjectTraversal {
  private final ReferenceResolver resolver;

  public DSObjectTraversal(final ReferenceResolver resolver) {
    this.resolver = resolver;
  }

  public void traverse(DSObject object) {

    if (object instanceof DSDictionary) {
      final DSDictionary dict = (DSDictionary) object;
      visitEnter(dict);

      dict.forEach(e -> {
        visitEntry(e.getKey(), e.getValue());
      });

      visitLeave(dict);
    }

  }

  public void visitEntry(final DSNameObject key, final DSObject value) {

  }

  public void visitEnter(DSDictionary dictionary) {

  }

  public void visitLeave(DSDictionary dictionary) {

  }

}

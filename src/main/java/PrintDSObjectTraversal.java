import java.io.PrintWriter;

import org.jadice.util.base.Strings;

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

public class PrintDSObjectTraversal extends DSObjectTraversal {
  private final int maxReferenceDepth;
  private final PrintWriter out;
  private String indentation = "";
  private boolean indentNext;

  public PrintDSObjectTraversal(final ReferenceResolver resolver, PrintWriter out, final int maxReferenceDepth) {
    super(resolver);
    this.out = out;
    this.maxReferenceDepth = maxReferenceDepth;
  }

  @Override
  public void visitEnter(final DSDictionary dictionary) {
    println("<<");
    incrementIndentation();
  }

  @Override
  public void visitEntry(final DSNameObject key, final DSObject value) {
    print("/" + key.getName() + " ");
    // the value will be printed separate
  }

  @Override
  public void visitLeave(final DSDictionary dictionary) {
    decrementIndentation();
    println(">>");
  }

  @Override
  public void visitItem(final int index, final DSObject item) {
    // the item will be visited separately
  }

  @Override
  public void visitLeave(final DSArray array) {
    decrementIndentation();
    println("]");
  }

  @Override
  public void visitEnter(final DSArray array) {
    println("[");
    incrementIndentation();
  }

  @Override
  public boolean visitReference(final DSReference reference, final int depth) {
    final boolean maxDepthReached = depth >= maxReferenceDepth;

    final String msg =
        "{" + reference.getReferencedObjectNumber() + " " + reference.getReferencedGenerationNumber() + " R} ";
    if (maxDepthReached)
      println(msg);
    else
      print(msg);
    return !maxDepthReached;
  }

  @Override
  public void visit(final DSBoolean object) {
    println("" + object.getValue());
  }

  @Override
  public void visit(final DSInteger object) {
    println("" + object.getInteger());
  }

  @Override
  public void visit(final DSReal object) {
    println("" + object.getDouble());
  }

  @Override
  public void visit(final DSNullObject object) {
    println("null");
  }

  @Override
  public void visit(final DSNameObject object) {
    println("/" + object.getName());
  }

  @Override
  public void visit(final DSLiteralString object) {
    println("(" + new String(object.getRawData()) + ")");
  }

  @Override
  public void visit(final DSHexString object) {
    println("<" + Strings.toHex(object.getRawData(), 0, object.getRawData().length) + ">");
  }

  protected void incrementIndentation() {
    indentation += "  ";
  }

  protected void decrementIndentation() {
    if (indentation.length() <= 2)
      indentation = "";
    else
      indentation = indentation.substring(0, indentation.length() - 2);
  }

  protected void print(String msg) {
    if (indentNext) {
      out.print(indentation);
      indentNext = false;
    }
    out.print(msg);
  }

  protected void println(String msg) {
    if (indentNext)
      out.print(indentation);
    out.println(msg);
    indentNext = true;
  }
}

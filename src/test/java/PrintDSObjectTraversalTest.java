import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import com.levigo.jadice.format.pdf.internal.ReferenceResolver;
import com.levigo.jadice.format.pdf.internal.objects.DS;
import com.levigo.jadice.format.pdf.internal.objects.DSArray;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;

public class PrintDSObjectTraversalTest {

  @Test
  void testFormatArray() {

    final DSArray in = DS.array( //
        DS.num(1), //
        DS.num(2), //
        DS.num(3) //
    );

    final String s = process(in);

    assertEquals("[\n" + "  1\n" + "  2\n" + "  3\n" + "]\n", s);

  }

  @Test
  void testFormatArrayWithReferences() {
    final DSArray in = DS.array( //
        DS.ref(1), //
        DS.ref(2), //
        DS.ref(3) //
    );

    final String s = process(in);

    assertEquals("[\n  {1 0 R} \n  {2 0 R} \n  {3 0 R} \n]\n", s);
  }

  @Test
  void testFormatDictionayWithArrayWithReferences() {
    final DSDictionary dict = DS.dict();

    dict.addNamedEntry(DS.name("A"), DS.array( //
        DS.ref(1), //
        DS.ref(2), //
        DS.ref(3) //
    ));

    final String s = process(dict);

    assertEquals("<<\n" + "  /A [\n" + "    {1 0 R} \n" + "    {2 0 R} \n" + "    {3 0 R} \n" + "  ]\n" + ">>\n", s);
  }


  private String process(final DSObject in) {
    final int maxReferenceDepth = 0;
    final StringWriter ws = new StringWriter();
    try (final PrintWriter out = new PrintWriter(ws)) {
      final PrintDSObjectTraversal traversal = new PrintDSObjectTraversal(new ReferenceResolver() {
        @Override
        public DSObject resolve(final DSObject ref) {
          return null;
        }
      }, out, maxReferenceDepth);
      traversal.traverse(in);
    }

    return ws.toString();
  }
}

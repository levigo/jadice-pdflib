import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.levigo.jadice.document.io.RandomAccessFileInputStream;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.pdf.internal.DefaultPDFDocumentFactory;
import com.levigo.jadice.format.pdf.internal.PDFDocument;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSNameObject;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;

public class Example {
  public static void main(String[] args) throws Exception {


    if (args.length != 1) {
      System.err.println("Incorrect number of arguments. Expecting a single path ot a PDF document");
      System.exit(1);
    }

    final Path inputFile = Paths.get(args[0]);

    if (!Files.isRegularFile(inputFile)) {
      System.err.println("Not a file: " + inputFile);
      System.exit(1);
    }

    try (final SeekableInputStream source = new RandomAccessFileInputStream(inputFile.toFile())) {

      final DefaultPDFDocumentFactory docFactory = new DefaultPDFDocumentFactory();
      final PDFDocument document = docFactory.create(source);

      System.out.println("PDF Document Format Version " + document.getDocumentStructure().getVersion());

      final DSDictionary catalog = document.getCatalog();

      new DSObjectTraversal(document.getResolver()) {
        @Override
        public void visitEnter(final DSDictionary dictionary) {
          System.out.println("<<");
        }

        @Override
        public void visitEntry(final DSNameObject key, final DSObject value) {
          System.out.println("  /" + key.getName() + " " + value);
        }

        @Override
        public void visitLeave(final DSDictionary dictionary) {
          System.out.println(">>");
        }
      }.traverse(catalog);


    }
  }
}

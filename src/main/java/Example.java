import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.levigo.jadice.document.io.RandomAccessFileInputStream;
import com.levigo.jadice.document.io.SeekableInputStream;
import com.levigo.jadice.format.pdf.internal.DefaultPDFDocumentFactory;
import com.levigo.jadice.format.pdf.internal.PDFDocument;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
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

      try (final PrintWriter pw = new PrintWriter(System.out)) {
        new PrintDSObjectTraversal(document.getResolver(), pw, 3).traverse(catalog);
      }


      // access the root page tree node
      // the Pages node has to be a reference. Due to this, we've got to resolve the reference
      final DSObject ref = catalog.getNamedEntryValue("Pages");
      final DSDictionary pageTreeNode = document.getResolver().resolveDictionary(ref);

    }
  }
}

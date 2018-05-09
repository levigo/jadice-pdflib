package com.levigo.jadice.format.pdf.internal.objects;

import com.levigo.jadice.document.io.SeekableInputStream;

public class DSStream extends DSObject {

  /**
   * contains the Dictionary for the stream
   */
  private final DSDictionary dictionary;
  private final IStreamFactory streamFactory;

  /**
   * initializes a <code>PDFStream</code> object, containing a {@link DSDictionary}with all the
   * stream relevant entries. Don't forget to set the area of the Stream data with
   * {@link #setDataArea(long, long)}
   *
   * @param dictionary the stream relevant dictionary
   */
  public DSStream(DSDictionary dictionary, IStreamFactory streamFactory) {
    this.dictionary = dictionary;
    this.streamFactory = streamFactory;
  }

  /**
   * returns the stream dictionary
   *
   * @return the dictionary
   */
  public DSDictionary getDictionary() {
    return this.dictionary;
  }

  public SeekableInputStream getSourceStream() {
    return streamFactory.createStream();
  }

  /**
   * <code>equals(PDFObject)</code> will only check for reference equality as comparing of stream
   * data is not very wise
   *
   * @see com.levigo.jadice.format.pdf.internal.objects.DSObject#equals(com.levigo.jadice.format.pdf.internal.objects.DSObject)
   */
  public boolean equals(DSObject object) {
    if (this == object)
      return true;
    return false;
  }

  /**
   * This method is mainly used for the PDF-DocInspector
   *
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "PDFStream";
  }
}

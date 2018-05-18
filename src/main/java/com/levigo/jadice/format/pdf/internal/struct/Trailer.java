package com.levigo.jadice.format.pdf.internal.struct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.levigo.jadice.format.pdf.internal.Utils;
import com.levigo.jadice.format.pdf.internal.objects.DSArray;
import com.levigo.jadice.format.pdf.internal.objects.DSDictionary;
import com.levigo.jadice.format.pdf.internal.objects.DSNumber;
import com.levigo.jadice.format.pdf.internal.objects.DSObject;
import com.levigo.jadice.format.pdf.internal.objects.DSString;

public class Trailer extends AbstractStructureObject<DSDictionary> {
  private static final Logger LOGGER = LoggerFactory.getLogger(Trailer.class);

  private final DSDictionary dictionary;

  private int size = -1;
  private long prev = -1;
  private DSString[] id;

  public Trailer(DSDictionary trailerDict) {
    super(trailerDict);
    this.dictionary = trailerDict;
  }

  /**
   * @return Returns the encrypt.
   */
  public DSObject getEncrypt() {
    return dictionary.getNamedEntryValue("Encrypt");
  }

  /**
   * @return Returns the iD.
   */
  public DSString[] getID() {

    if (id == null) {

      DSArray array = Utils.getArray(dictionary.getNamedEntryValue("ID"));

      if (array != null) {
        if (array.size() == 2 && array.get(0) instanceof DSString && array.get(1) instanceof DSString) {

          id = new DSString[]{(DSString) array.get(0), (DSString) array.get(1),
          };
        } else {
          // something is weird
          LOGGER.warn("found malformed ID entry in the document trailer");
        }
      }

    }

    return id;
  }

  public DSDictionary getDocumentInformation() {
    return Utils.getDictionary(dictionary.getNamedEntryValue("Info"));
  }

  /**
   * @return Returns the prev.
   */
  public long getPrev() {
    if (prev < 0) {
      DSNumber tmp = Utils.getNumber(dictionary.getNamedEntryValue("Prev"));
      if (tmp != null)
        prev = tmp.getLong();
    }
    return prev;
  }

  /**
   * @return Returns the root.
   */
  public DSObject getRoot() {
    return dictionary.getNamedEntryValue("Root");
  }

  /**
   * @return Returns the size.
   */
  public int getSize() {
    if (size < 0) {
      DSNumber tmp = Utils.getNumber(dictionary.getNamedEntryValue("Size"));
      if (tmp != null)
        size = tmp.getInteger();
    }
    return size;
  }

  public DSDictionary getDictionary() {
    return dictionary;
  }
}

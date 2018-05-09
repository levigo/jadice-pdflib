package com.levigo.jadice.format.pdf.internal.objects;


/**
 * A PDF Name Object.
 * <p>
 * Note that name objects are defined as
 * "an atomic symbol uniquely defined by a sequence of any characters (8-bit values) except null (character code 0)"
 * (Source: PDF 32000-1:2008, 7.3.5 "Name Objects"). We store the name object's contents as a
 * String. If each String uses the same encoding this should be OK.
 */
public class DSNameObject extends DSObject implements Comparable<DSNameObject> {

  public static final DSNameObject type = new DSNameObject("Type");
  public static final DSNameObject filter = new DSNameObject("Filter");
  public static final DSNameObject bitsPerComponent = new DSNameObject("BitsPerComponent");
  public static final DSNameObject colorSpace = new DSNameObject("ColorSpace");
  public static final DSNameObject decode = new DSNameObject("Decode");
  public static final DSNameObject decodeParms = new DSNameObject("DecodeParms");
  public static final DSNameObject height = new DSNameObject("Height");
  public static final DSNameObject width = new DSNameObject("Width");
  public static final DSNameObject deviceGray = new DSNameObject("DeviceGray");
  public static final DSNameObject deviceRGB = new DSNameObject("DeviceRGB");
  public static final DSNameObject deviceCMYK = new DSNameObject("DeviceCMYK");
  public static final DSNameObject indexed = new DSNameObject("Indexed");
  public static final DSNameObject iccBased = new DSNameObject("ICCBased");
  public static final DSNameObject asciiHexDecode = new DSNameObject("ASCIIHexDecode");
  public static final DSNameObject ascii85Decode = new DSNameObject("ASCII85Decode");
  public static final DSNameObject lzwDecode = new DSNameObject("LZWDecode");
  public static final DSNameObject flateDecode = new DSNameObject("FlateDecode");
  public static final DSNameObject ccittFaxDecode = new DSNameObject("CCITTFaxDecode");
  public static final DSNameObject dctDecode = new DSNameObject("DCTDecode");
  public static final DSNameObject runLengthDecode = new DSNameObject("RunLengthDecode");
  public static final DSNameObject calRGB = new DSNameObject("CalRGB");
  public static final DSNameObject calGray = new DSNameObject("CalGray");
  public static final DSNameObject font = new DSNameObject("Font");
  public static final DSNameObject fontName = new DSNameObject("FontName");
  public static final DSNameObject fontFamily = new DSNameObject("FontFamily");
  public static final DSNameObject flags = new DSNameObject("Flags");
  public static final DSNameObject fontBBox = new DSNameObject("FontBBox");
  public static final DSNameObject fontMatrix = new DSNameObject("FontMatrix");
  public static final DSNameObject leading = new DSNameObject("Leading");
  public static final DSNameObject separation = new DSNameObject("Separation");
  public static final DSNameObject differences = new DSNameObject("Differences");
  public static final DSNameObject fontDescriptor = new DSNameObject("FontDescriptor");
  public static final DSNameObject subtype = new DSNameObject("Subtype");
  public static final DSNameObject pattern = new DSNameObject("Pattern");
  public static final DSNameObject extGState = new DSNameObject("ExtGState");
  /**
   * the name HAS to be stored without trailing /
   */
  private final String name;

  public DSNameObject(String name) {
    // check if there is a preceding /
    if (name.length() > 0 && name.charAt(0) == '/') {
      // set name only with the name and omit the /
      name = name.substring(1, name.length());
    }

    // use the canonical representation with help of String.intern(), which does some pooling
    // FIXME DOCPV-1766 do we really want to intern() every name String
    this.name = name.intern();
  }

  /**
   * checks for value equality.
   *
   * @param dsObject the {@link DSObject} to compare with
   * @return <code>true</code> if equal, otherwise <code>false</code>
   * @see #matches(String) if a comparison with a <code>String</code> is needed, use
   * {@link #matches(String)} instead
   */
  @Override
  public boolean equals(DSObject dsObject) {
    if (dsObject instanceof DSNameObject) {
      return name.equals(((DSNameObject) dsObject).name);
    }
    return false;
  }

  /**
   * compare a <code>String</code> with this <code>PDFNameObject</code>. To increase performance do
   * not use Strings with trailing /
   *
   * @param compareWith is a string with which the check should be run
   * @return <code>true</code> if the nameobject matches with value. If not <code>false</code>
   */
  public boolean matches(String compareWith) {
    // the name is absolutely equal
    if (name.equals(compareWith)) {
      return true;
    }
    // check if the compareWith was passed as an Argument with preceding /
    // FIXME not really fast
    if (compareWith.startsWith("/") && name.length() + 1 == compareWith.length()) {
      for (int i = 1; i < name.length(); i++) {
        if (name.charAt(i - 1) != compareWith.charAt(i)) {
          return false;
        }
      }
      return true;
    }

    return false;
  }

  /**
   * returns a {@link String} of the format:
   *
   * <pre>
   *     &lt;Name&gt; (Name Object)
   * </pre>
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return name + "(Name Object)";
  }

  @Override
  public int compareTo(DSNameObject o) {
    return name.compareTo((o).getName());
  }

  /**
   * @return <code>true</code> for this class and all derived classes
   */
  @Override
  public final boolean isNameObject() {
    return true;
  }

  /**
   * @return the name without trailing slash
   */
  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

}

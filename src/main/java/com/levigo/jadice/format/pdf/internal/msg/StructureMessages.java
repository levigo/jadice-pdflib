package com.levigo.jadice.format.pdf.internal.msg;

public class StructureMessages {

  public static final String FORMAT_VERSION_DETECTION_FAIL = "unable to read out PDF document version";


  public static final String UNEXPECTED_TOKEN = "found unexpected syntactical element while reading";


  public static final String STREAM_WITHOUT_DICTIONARY = "illegal content: stream object has not dictionary.";


  public static final String STREAM_LENGTH_MISSING = "missing required length for a stream object.";


  public static final String STREAM_KEYWORD_TERMINATED_CR = "stream keyword has been terminated with CARRIAGE RETURN without LINE FEED.";


  public static final String STREAM_KEYWORD_INCORRECTLY_TERMINATED = "stream keyword has not been terminated by either CARRIAGE RETURN and LINE FEED, nor a single LINE FEED";


  public static final String UNEXPECTED_CHARACTER = "found unexpected characters. This typically means that there are syntactical problems.";


  public static final String ILLEGAL_XREF_OFFSET = "Illegal offset of cross reference table ({0})";


  public static final String MISSING_EOF_MARKER = "no %%EOF marker has been found.";


  public static final String EXPECTED_XREFSTREAM = "expected cross reference stream. Structure is corrupt.";


  public static final String NO_XREFTABLE_AT_OFFSET = "specified location does not contain a cross reference table";


  public static final String XREFT_EITHER_N_OR_F_EXPECTED = "cross reference table contained illegal content. Either used or free markers are allowed.";


  public static final String XREFT_CORRUPT = "cross reference table is corrupt";


  public static final String XREFS_MISSING_W_ARRAY = "the cross reference stream is missing the W array.";


  public static final String XREFS_W_INCORRECT_CONTENT = "incorrect element in W array";


  public static final String XREFS_INDEX_ARRAY_ERROR = "the index array of a cross reference stream dictionary is incorrect";


  public static final String XREFS_SIZE_MISSING = "mandatory 'Size' entry in cross reference stream missing";


  public static final String XREFS_SIZE_ERROR = "mandatory 'Size' entry in cross reference stream incorrect";


  public static final String XREFS_STREAM_CORRUPT = "cross reference stream data corrupted";


  public static final String OBJSTM_N_MISSING = "the number of objects inside object stream is missing";


  public static final String OBJSTM_FIRST_MISSING = "the offset to the first object in object stream is missing";


  public static final String PAGETREE_MISSING_TYPE = "missing type declaration in page tree. Trying to detect.";


  public static final String PAGETREE_ILLEGAL_ENTRY = "illegal entry in page tree";


  public static final String MEDIA_BOX_CORRUPT = "MediaBox of page is corrupt. May be displayed broken";


  public static final String MEDIA_BOX_MISSING = "MediaBox of page is missing.";


  public static final String CROP_BOX_CORRUPT = "CropBox of page is corrupt. May be displayed incorrect";


  public static final String BLEED_BOX_CORRUPT = "BleedBox of page is corrupt.";


  public static final String TRIM_BOX_CORRUPT = "TrimBox of page is corrupt.";


  public static final String ART_BOX_CORRUPT = "ArtBox of page is corrupt.";


  public static final String PAGE_ILLEGAL_CONTENTS = "non conforming type of page data found, which will be omitted. Document may be displayed broken";


  public static final String PAGE_ROTATION_INVALID = "invalid pdf page rotation specified: {0}. Defaulting to zero degrees.";


  public static final String CATALOG_MISSING_PAGES = "Missing /Pages in document catalog. No pages can be read from the document.";


  public static final String TRAILER_ID_MALFORMED = "found malformed ID entry in the document trailer";


  public static final String MISSING_STARTXREF = "no startxref keyword found. Failed to load the document.";


  public static final String XREF_TABLE_OFFSET_MISSING = "missing cross reference table location.";


  public static final String MISSING_ENDSTREAM_KEYWORD = "Missing endstream keyword after stream data";


  public static final String MISSING_ENDOBJ_KEYWORD = "Missing endobj keyword after body of object {0} {1}";


  public static final String INCORRECT_XREF_TABLE = "Incorrectly formulated cross reference table. Found object {2} {3} instead of object {0} {1}";


  public static final String STREAM_KEYWORD_TRAILING_SPACE = "The stream keyword is followed by space character(s). It must be terminated by either CARRIAGE RETURN and LINE FEED, nor a single LINE FEED";


  public static final String FAILED_TREE_LOADING = "Failed to load tree";

}

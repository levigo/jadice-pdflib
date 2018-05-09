package com.levigo.jadice.format.pdf.internal.msg;

public class ContentStreamMessages {

  public static final String INVALID_CHARACTER = "invalid character at offset {0}. Hex value of character: 0x{1}";


  public static final String ILLEGAL_CONTENT_SEQUENCE = "illegal content sequence at offset {0}. Hex value of content sequence: 0x{1}";


  public static final String ILLEGAL_COLOR_VALUE_FOR_COMPONENT = "operator {0} specified illegal color value {1} for component {2}. Valid range [{3},{4}]";


  public static final String ILLEGAL_SCOPE_FOR_OPERATION = "Illegal scope {0} for operation {1}.";


  public static final String MISSING_PATH_OBJECT_FOR_OPERATION = "Missing path object for operation {0}.";


  public static final String ILLEGAL_LINE_CAP_SIGNALLED = "Illegal line cap signalled. Valid values are 0 (butt), 1 (round), 2 (square) but was {0}.";


  public static final String ILLEGAL_LINE_JOIN_SIGNALLED = "Illegal line join signalled. Valid values are 0 (miter), 1 (round), 2 (bevel) but was {0}.";


  public static final String ILLEGAL_LINE_WIDTH_SIGNALLED = "Illegal line width signalled. Value must be greater or equal to 0 but was {0}.";


  public static final String MISSING_OPERANDS_FOR_OPERATION = "Missing operands for {0}.";


  public static final String MISSING_FONT = "Missing font for Show Text Operation.";


  public static final String MISSING_FONT_SIZE = "Missing font size for Show Text Operation.";


  public static final String MISSING_COLORSPACE = "Missing color space {0}.";


  public static final String USING_FALLBACK_COLOR = "Using fallback color.";


  public static final String MISSING_FONT_DICTIONARY = "Missing font dictionary for {0}.";


  public static final String NO_GLYPHDATA_DECODED = "No glyphdata decoded for {0}.";


  public static final String WINDING_RULE_NOT_SPECIFIED = "No winding rule was specified. Picking default: {0}";


  public static final String ILLEGAL_EXECUTOR_FOR_OPERATION = "Illegal executor implementation mapped for {0}.";

}

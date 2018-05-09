package com.levigo.jadice.format.pdf.internal.parsing;

import com.levigo.jadice.format.ps.internal.TokenTypes;

public interface TokenTypesPDF extends TokenTypes {

  /*
   * NOTE: ENSURE THAT THESE IDENTIFIERS REFER TO AN ALREADY DEFINED TOKEN_TYPE (TOKEN_TYPE_STRING,
   * TOKEN_TYPE_NUMBER) AND ARE UNIQUE
   */
  public static final long REAL = TokenTypes.TOKEN_TYPE_NUMBER | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID));

  public static final long INT = TokenTypes.TOKEN_TYPE_NUMBER | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 1));

  public static final long NAME = TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 2));

  public static final long ARRAY_BEGIN = TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 3));

  public static final long ARRAY_END = TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 4));

  public static final long HEX_STRING = TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 5));

  public static final long KEYWORD_TRUE = TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 6));

  public static final long KEYWORD_FALSE = TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 7));

  public static final long KEYWORD_NULL = TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 8));

  public static final long LITERAL_STRING = TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 9));

  public static final long DICTIONARY_BEGIN =
      TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 10));

  public static final long DICTIONARY_END = TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 11));

  public static final long KEYWORD_OBJECT = TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 12));

  public static final long KEYWORD_ENDOBJECT =
      TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 13));

  public static final long KEYWORD_STREAM = TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 14));

  public static final long KEYWORD_ENDSTREAM =
      TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 15));

  public static final long REF = TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 16));

  public static final long PAGE_CONTENT_OPERATOR =
      TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 17));

  public static final long FREE_TOKEN_TYPE_ID = TokenTypesPDF.FREE_TOKEN_TYPE_ID + 18;
}

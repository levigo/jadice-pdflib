package com.levigo.jadice.format.ps.internal;

public interface TokenTypesPS extends TokenTypes {

  /*
   * NOTE: ENSURE THAT THESE IDENTIFIERS REFER TO AN ALREADY DEFINED TOKEN_TYPE
   * (TOKEN_TYPE_STRING, TOKEN_TYPE_NUMBER) AND ARE UNIQUE
   */
  public static final long TOKEN_TYPE_REAL = TokenTypes.TOKEN_TYPE_NUMBER | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID));

  public static final long TOKEN_TYPE_INT = TokenTypes.TOKEN_TYPE_NUMBER | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 1));

  public static final long TOKEN_TYPE_NAME = TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 2));

  public static final long TOKEN_TYPE_ARRAY_BEGIN =
      TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 3));

  public static final long TOKEN_TYPE_ARRAY_END =
      TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 4));

  public static final long TOKEN_TYPE_HEX_STRING =
      TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 5));

  public static final long TOKEN_TYPE_KEYWORD =
      TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 6));

  public static final long TOKEN_TYPE_LITERAL_STRING =
      TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 7));

  public static final long TOKEN_TYPE_EXEC_ARRAY_BEGIN =
      TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 8));

  public static final long TOKEN_TYPE_EXEC_ARRAY_END =
      TokenTypes.TOKEN_TYPE_STRING | (1 << (TokenTypes.FREE_TOKEN_TYPE_ID + 9));

  /**
   * this is the first free ps tokentype ID. All derived token types should start
   * their tokentype ID by doing a leftshift by the value of FREE_TOKEN_TYPE_ID
   */
  public static final long FREE_TOKEN_TYPE_PS_ID = TokenTypes.FREE_TOKEN_TYPE_ID << 10;

  //	public static final long TOKEN_TYPE_REAL = TokenTypes.TOKEN_TYPE_NUMBER
  //	| (TokenTypes.FREE_TOKEN_TYPE_ID << 1);
  //
  //	public static final long TOKEN_TYPE_INT = TokenTypes.TOKEN_TYPE_NUMBER
  //	| (TokenTypes.FREE_TOKEN_TYPE_ID << 2);
  //
  //	public static final long TOKEN_TYPE_NAME = TokenTypes.TOKEN_TYPE_STRING
  //			| (TokenTypes.FREE_TOKEN_TYPE_ID << 3);
  //
  //	public static final long TOKEN_TYPE_ARRAY_BEGIN = TokenTypes.TOKEN_TYPE_STRING
  //			| (TokenTypes.FREE_TOKEN_TYPE_ID << 4);
  //
  //	public static final long TOKEN_TYPE_ARRAY_END = TokenTypes.TOKEN_TYPE_STRING
  //			| (TokenTypes.FREE_TOKEN_TYPE_ID << 5);
  //
  //	public static final long TOKEN_TYPE_HEX_STRING = TokenTypes.TOKEN_TYPE_STRING
  //			| (TokenTypes.FREE_TOKEN_TYPE_ID << 6);
  //
  //	public static final long TOKEN_TYPE_KEYWORD = TokenTypes.TOKEN_TYPE_STRING
  //			| (TokenTypes.FREE_TOKEN_TYPE_ID << 7);
  //
  //	public static final long TOKEN_TYPE_LITERAL_STRING = TokenTypes.TOKEN_TYPE_STRING
  //			| (TokenTypes.FREE_TOKEN_TYPE_ID << 8);
  //
  //	public static final long TOKEN_TYPE_EXEC_ARRAY_BEGIN = TokenTypes.TOKEN_TYPE_STRING
  //			| (TokenTypes.FREE_TOKEN_TYPE_ID << 9);
  //
  //	public static final long TOKEN_TYPE_EXEC_ARRAY_END = TokenTypes.TOKEN_TYPE_STRING
  //			| (TokenTypes.FREE_TOKEN_TYPE_ID << 10);

}

package com.levigo.jadice.format.ps.internal;

public interface TokenTypes {

  /**
   * token type which indicates that the end of file has been reached
   */
  public static final long TOKEN_TYPE_EOF = 1;

  /**
   * token type indicating an end of line
   */
  public static final long TOKEN_TYPE_EOL = 1 << 1;

  /**
   * base tokentype for all string tokens
   */
  public static final long TOKEN_TYPE_STRING = 1 << 2;

  /**
   * base tokentype for all number tokens
   */
  public static final long TOKEN_TYPE_NUMBER = 1 << 3;

  /**
   * this is the first free tokentype ID. All derived token types should start
   * their tokentype ID by doing a leftshift by the value of FREE_TOKEN_TYPE_ID
   */
  public static final long FREE_TOKEN_TYPE_ID = 4;

}

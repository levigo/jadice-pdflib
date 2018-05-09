package com.levigo.jadice.format.ps.internal;

import java.io.IOException;


public interface TokenProvider {

  public abstract Token getNextToken() throws IOException;

  /**
   * peeks a token. <code>tokenNumber</code> specifies the token to be peeked,
   * where <code>tokenNumber = 0</code> will give the same result as
   * {@link #getNextToken()} with the speciality that a call to
   * <code>peekToken(0)</code> followed by a {@link #getNextToken()} will give
   * the same result. <code>peekToken</code> does not consume the token beeing
   * looked ahead.
   *
   * @param tokenNumber
   * @return
   */
  public abstract Token peekToken(int tokenNumber) throws IOException;

}

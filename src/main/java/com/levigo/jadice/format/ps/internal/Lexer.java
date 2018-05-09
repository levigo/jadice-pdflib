package com.levigo.jadice.format.ps.internal;

import java.io.IOException;

import com.levigo.jadice.document.io.SeekableLookaheadStream;

public abstract class Lexer implements TokenProvider {
  public static final Token EOF_TOKEN = new EmptyToken(TokenTypes.TOKEN_TYPE_EOF);
  public static final Token EOL_TOKEN = new EmptyToken(TokenTypes.TOKEN_TYPE_EOF);


  public static class EmptyToken extends Token {

    public EmptyToken(long tokenType) {
      super(tokenType);
    }

    @Override
    public Number getNumberToken() {
      return null;
    }

    @Override
    public byte[] getStringToken() {
      return null;
    }

  }

  private final boolean validateTokenProvider;
  private StatefulTokenProvider tokenProvider;

  protected Lexer(boolean validateTokenProvider) {
    this.validateTokenProvider = validateTokenProvider;
    initTokenProvider();
  }

  public final Token getNextToken() throws IOException {
    /*
     * check if we should validate the token provider. If the tokenProvider is
     * invalid call initTokenProvider to fetch a new instance (derived classes
     * have to implement the Method getTokenProvider()
     */
    if (validateTokenProvider && !tokenProvider.isValid()) {
      initTokenProvider();
    }

    /*
     * delegating the call to the tokenprovider
     */
    return tokenProvider.getNextToken();
  }

  public final Token peekToken(int tokenNumber) throws IOException {
    /*
     * see method getNextToken() for description
     */
    if (validateTokenProvider && !tokenProvider.isValid()) {
      initTokenProvider();
    }

    return tokenProvider.peekToken(tokenNumber);
  }

  private final void initTokenProvider() {
    tokenProvider = getTokenProvider();
  }

  protected abstract StatefulTokenProvider getTokenProvider();

  public abstract void seek(long pos) throws IOException;

  public abstract SeekableLookaheadStream getLookaheadStream();
}

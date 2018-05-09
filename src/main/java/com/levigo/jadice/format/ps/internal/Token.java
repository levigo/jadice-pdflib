package com.levigo.jadice.format.ps.internal;

public abstract class Token {
  protected long tokenType;
  long startOffset;
  long endOffset;

  public Token(long tokenType) {
    this.tokenType = tokenType;
  }

  public boolean isOfType(long type) {
    return (this.tokenType & type) == type;
  }

  public long getTokenType() {
    return this.tokenType;
  }

  public abstract byte[] getStringToken();

  public abstract Number getNumberToken();

  protected void setOffsets(long start, long end) {
    this.startOffset = start;
    this.endOffset = end;
  }

  protected final long getEndOffset() {
    return endOffset;
  }

  protected final long getStartOffset() {
    return startOffset;
  }

  public boolean isLiteral() {
    return isOfType(TokenTypesPS.TOKEN_TYPE_NUMBER) // all possible number
        // tokens
        || isOfType(TokenTypesPS.TOKEN_TYPE_NAME) || isOfType(TokenTypesPS.TOKEN_TYPE_HEX_STRING) || isOfType(
        TokenTypesPS.TOKEN_TYPE_LITERAL_STRING);
  }
}

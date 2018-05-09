package com.levigo.jadice.format.pdf.internal.parsing;

import com.levigo.jadice.format.pdf.internal.msg.StructureMessages;
import com.levigo.jadice.format.ps.internal.Token;

public abstract class AbstractParserSupport {

  public AbstractParserSupport() {
    super();
  }

  protected final void assertTokenType(Token token, long tokenType) {
    if (!token.isOfType(tokenType)) {
      throw new RuntimeException(StructureMessages.UNEXPECTED_TOKEN);
    }
  }

}

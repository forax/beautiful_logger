package com.github.forax.beautifullogger.tool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * Base64 encodes the class ReflectionSupplier.class so it can be embedded easily in the code.
 */
public final class ReflectionSupplierEncoder {
  private ReflectionSupplierEncoder() {
    throw new AssertionError();
  }

  /**
   * Base64 encodes the class ReflectionSupplier.class so it can be embedded easily in the code.
   *
   * @param args not used.
   * @throws IOException if an i/o error occurs.
   */
  public static void main(String[] args) throws IOException {
    //InputStream input = ReflectionSupplierEncoder.class.getResourceAsStream("ReflectionSupplier.class");
    //byte[] code = input.readAllBytes();
    //byte[] rewritedCode = Rewriter.rewrite(code);
    //System.out.println(Base64.getEncoder().encodeToString(rewritedCode));
  }
}


/*package com.github.forax.beautifullogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import com.github.forax.beautifullogger.tool.Rewriter;

public class ReflectionSupplierEncoder {
  public static void main(String[] args) throws IOException {
    InputStream input = ReflectionSupplierEncoder.class.getResourceAsStream("ReflectionSupplier.class");
    byte[] code = input.readAllBytes();
    byte[] rewritedCode = Rewriter.rewrite(code);
    System.out.println(Base64.getEncoder().encodeToString(rewritedCode));
  }
}*/

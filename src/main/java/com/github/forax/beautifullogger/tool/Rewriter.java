package com.github.forax.beautifullogger.tool;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * Rewrite the bytecode to be Java 8 compatible
 */
public final class Rewriter {
  /**
   * Rewrite the bytecode pass as parameter to be java 8 compatible.
   * @param code the bytecode to be rewritten.
   * @return a Java 8 compatible bytecode.
   * @throws IOException if an i/o occurs.
   */
  public static byte[] rewrite(byte[] code) throws IOException {
    ClassReader reader = new ClassReader(code);
    ClassWriter writer = new ClassWriter(reader, 0);
    
    reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
      @Override
      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        int newVersion = name.equals("module-info")? Opcodes.V9: Opcodes.V1_8;
        super.visit(newVersion, access, name, signature, superName, interfaces);
      }
    }, ClassReader.SKIP_CODE);
    
    return writer.toByteArray();
  }

  /**
   * Rewrite the bytecode of the library to be Java 8 compatible.
   * This allows to use new VM features like anonymous classes or hidden classes
   * that may be available at runtime while still being Java 8 compatible.
   * @param args not used.
   * @throws IOException if an i/o error occurs.
   */
  public static void main(String[] args) throws IOException {
    Path directory = Paths.get(args[0]);
    System.out.println("rewrite " + directory + " to Java 8");
    
    try(Stream<Path> stream = Files.walk(directory)) {
      stream
        .filter(path -> path.getFileName().toString().endsWith(".class"))
        .forEach(path -> {
          boolean tool = path.toString().contains("com/github/forax/beautifullogger/tool");
          try {
            if (tool) {
              System.out.println("delete " + path);
              Files.delete(path);
              return;
            }

            System.out.println("rewrite " + path);
            Files.write(path, rewrite(Files.readAllBytes(path)));
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
    } catch(UncheckedIOException e) {
      throw e.getCause();
    }
  }
}

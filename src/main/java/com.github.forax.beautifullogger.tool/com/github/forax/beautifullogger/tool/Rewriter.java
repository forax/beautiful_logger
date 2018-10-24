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
public class Rewriter {
  public static byte[] rewrite(byte[] code) throws IOException {
    ClassReader reader = new ClassReader(code);
    ClassWriter writer = new ClassWriter(reader, 0);
    
    reader.accept(new ClassVisitor(Opcodes.ASM7, writer) {
      @Override
      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        int newVersion = name.equals("module-info")? version: Opcodes.V1_8;
        super.visit(newVersion, access, name, signature, superName, interfaces);
      }
    }, ClassReader.SKIP_CODE);
    
    return writer.toByteArray();
  }
  
  public static void main(String[] args) throws IOException {
    Path directory = Paths.get(args[0]);
    System.out.println("rewrite " + directory + " to Java 8");
    
    try(Stream<Path> stream = Files.walk(directory)) {
      stream
        .filter(path -> path.getFileName().toString().endsWith(".class"))
        .forEach(path -> {
          try {
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

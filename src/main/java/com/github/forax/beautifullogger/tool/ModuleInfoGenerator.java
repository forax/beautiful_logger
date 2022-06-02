package com.github.forax.beautifullogger.tool;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ModuleVisitor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.ACC_MANDATED;
import static org.objectweb.asm.Opcodes.ACC_MODULE;
import static org.objectweb.asm.Opcodes.ACC_STATIC_PHASE;
import static org.objectweb.asm.Opcodes.V9;

public class ModuleInfoGenerator {
  public static byte[] generate() {

    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    classWriter.visit(V9, ACC_MODULE, "module-info", null, null, null);
    classWriter.visitSource("module-info.java", null);

    ModuleVisitor moduleVisitor =
        classWriter.visitModule("com.github.forax.beautifullogger", 0, "0.10.6");

    moduleVisitor.visitRequire("java.base", ACC_MANDATED, null);
    moduleVisitor.visitRequire("jdk.unsupported", ACC_STATIC_PHASE, null);

    moduleVisitor.visitRequire("org.apache.logging.log4j", ACC_STATIC_PHASE, "2.17.2");
    moduleVisitor.visitRequire("org.slf4j", ACC_STATIC_PHASE, "2.0.0-alpha7");
    moduleVisitor.visitRequire("ch.qos.logback.classic", ACC_STATIC_PHASE, "1.3.0-alpha16");
    moduleVisitor.visitRequire("ch.qos.logback.core", ACC_STATIC_PHASE, "1.3.0-alpha16");
    moduleVisitor.visitRequire("java.logging", ACC_STATIC_PHASE, null);
    //moduleVisitor.visitRequire("org.objectweb.asm", ACC_STATIC_PHASE, "9.3.0");

    moduleVisitor.visitExport("com/github/forax/beautifullogger", 0);
    moduleVisitor.visitEnd();

    classWriter.visitEnd();

    return classWriter.toByteArray();
  }

  public static void main(String[] args) throws IOException {
    Path directory = Paths.get(args[0]);

    System.out.println("generate module-info in " + directory);
    Files.write(directory.resolve("module-info.class"), generate());

    System.out.println("remove 'tool' classes in " + directory);
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
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });
    } catch(UncheckedIOException e) {
      throw e.getCause();
    }
  }
}

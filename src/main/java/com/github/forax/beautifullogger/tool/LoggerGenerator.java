package com.github.forax.beautifullogger.tool;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.objectweb.asm.ClassReader.SKIP_CODE;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASM6;
import static org.objectweb.asm.Opcodes.ASM7;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Locale;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class LoggerGenerator {
  public static void main(String[] args) throws IOException {
    ClassWriter writer = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    
    // reserve slot 1 of the constant pool (see LoggerServiceSPI)
    writer.newClass("com/github/forax/beautifullogger/Logger");
    
    writer.visit(V1_8, ACC_SUPER,
        "com/github/forax/beautifullogger/Logger$Stub", null,
        "java/lang/Object",
        new String[] {"com/github/forax/beautifullogger/Logger"});
    
    // field
    writer.visitField(ACC_PRIVATE|ACC_FINAL, "mh", "Ljava/lang/invoke/MethodHandle;", null, null);
    
    // constructor
    MethodVisitor init = writer.visitMethod(ACC_PRIVATE, "<init>", "(Ljava/lang/invoke/MethodHandle;)V", null, null);
    init.visitCode();
    init.visitVarInsn(ALOAD, 0);
    init.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    init.visitVarInsn(ALOAD, 0);
    init.visitVarInsn(ALOAD, 1);
    init.visitFieldInsn(PUTFIELD, "com/github/forax/beautifullogger/Logger$Stub", "mh", "Ljava/lang/invoke/MethodHandle;");
    init.visitInsn(RETURN);
    init.visitMaxs(0, 0);
    init.visitEnd();
    
    // static factory method
    MethodVisitor factory = writer.visitMethod(ACC_PUBLIC|ACC_STATIC, "create",
        "(Ljava/lang/invoke/MethodHandle;)Lcom/github/forax/beautifullogger/Logger;", null, null);
    factory.visitCode();
    factory.visitTypeInsn(NEW, "com/github/forax/beautifullogger/Logger$Stub");
    factory.visitInsn(DUP);
    factory.visitVarInsn(ALOAD, 0);
    factory.visitMethodInsn(INVOKESPECIAL, "com/github/forax/beautifullogger/Logger$Stub", "<init>",
        "(Ljava/lang/invoke/MethodHandle;)V", false);
    factory.visitInsn(ARETURN);
    factory.visitMaxs(0, 0);
    factory.visitEnd();
    
    // method
    generateOverride(writer,
        Paths.get("target/main/exploded/com.github.forax.beautifullogger/com/github/forax/beautifullogger/Logger.class"));
    generateOverride(writer,
        Paths.get("target/main/exploded/com.github.forax.beautifullogger/com/github/forax/beautifullogger/LogService.class"));
    
    writer.visitEnd();
    
    byte[] array = writer.toByteArray();
    
    //DEBUG
    //Files.write(Paths.get("Logger$Stub.class"), array);
    
    String data = new String(Base64.getEncoder().encode(array), ISO_8859_1);
    System.out.println(data);
  }

  private static void generateOverride(ClassWriter writer, Path loggerClass) throws IOException {
    ClassReader reader = new ClassReader(Files.readAllBytes(loggerClass));
    reader.accept(new ClassVisitor(ASM7) {
      @Override
      public void visitInnerClass(String name, String outerName, String innerName, int access) {
        // skip inner class
      }
      @Override
      public void visitNestMember(String nestMember) {
        // skip nest member
      }
      
      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ((access & ACC_ABSTRACT) == 0) {  // implement abstract method
          return null;
        }
        
        MethodVisitor mv = writer.visitMethod(access & (~ACC_ABSTRACT), name, desc, signature, exceptions);
        // old Hidden annotation, up to jdk 12
        mv.visitAnnotation("Ljava/lang/invoke/LambdaForm$Hidden;", true)
          .visitEnd();
        // new Hidden annotation, jdk 13+
        mv.visitAnnotation("Ljdk/internal/vm/annotation/Hidden;", true)
          .visitEnd();
        mv.visitAnnotation("Ljdk/internal/vm/annotation/ForceInline;", true)
          .visitEnd();
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "com/github/forax/beautifullogger/Logger$Stub", "mh",
            "Ljava/lang/invoke/MethodHandle;");
        
        if (!name.equals("log")) {
          mv.visitFieldInsn(GETSTATIC, "com/github/forax/beautifullogger/Logger$Level", name.toUpperCase(Locale.ROOT),
              "Lcom/github/forax/beautifullogger/Logger$Level;");
        }
        
        switch(Type.getArgumentTypes(desc)[0].getDescriptor()) {
          case "Lcom/github/forax/beautifullogger/Logger$Level;":  // log
            for(int i = 0; i < 7; i++) {
              mv.visitVarInsn(ALOAD, i + 1);
            }
            break;
          case "Ljava/lang/String;":  // message + throwable
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 1);
            genNONE(mv, 4);
            break;
          case "Ljava/util/function/Supplier;":
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ALOAD, 1);
            genNONE(mv, 4);
            break;
          case "Ljava/util/function/Function;":
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            genNONE(mv, 3);
            break;
          case "Ljava/util/function/IntFunction;":
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            genNONE(mv, 3);
            break;
          case "Ljava/util/function/LongFunction;":
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(LLOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            genNONE(mv, 3);
            break;
          case "Ljava/util/function/DoubleFunction;":
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(DLOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
            genNONE(mv, 3);
            break;
          case "Ljava/util/function/BiFunction;":
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            genNONE(mv, 2);
            break;
          default:
            throw new AssertionError("invalid method descriptor " + name + desc);
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invokeExact",
            "(Lcom/github/forax/beautifullogger/Logger$Level;Ljava/lang/Throwable;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V",
            false);
        
        mv.visitInsn(RETURN);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
        
        return null;
      }
      
      private void genNONE(MethodVisitor mv, int count) {
        for(int i = 0; i < count; i++) {
          mv.visitFieldInsn(Opcodes.GETSTATIC, "com/github/forax/beautifullogger/LoggerImpl", "NONE", "Ljava/lang/Object;");
        }
      }
      
    }, SKIP_CODE);
  }
}

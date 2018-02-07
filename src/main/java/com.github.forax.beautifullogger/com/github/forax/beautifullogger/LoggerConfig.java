package com.github.forax.beautifullogger;

import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.CLASS;
import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.MODULE;
import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.PACKAGE;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.function.Consumer;

import com.github.forax.beautifullogger.Logger.Level;

public interface LoggerConfig {
  @FunctionalInterface
  interface Printer {
    void print(String message, Level level, Throwable context);
  }
  
  @FunctionalInterface
  interface PrintFactory {
    MethodHandle getPrintMethodHandle(Class<?> configClass);
    
    static PrintFactory printer(Printer printer) {
      MethodHandle mh;
      try {
        mh = MethodHandles.lookup().findVirtual(Printer.class, "print",
            MethodType.methodType(void.class, String.class, Level.class, Throwable.class)); 
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
      MethodHandle target = mh.bindTo(printer);
      return __ -> target;
    }
    
    static PrintFactory systemLogger() {
      MethodHandle mh, filter;
      try {
        mh = MethodHandles.publicLookup().findVirtual(System.Logger.class, "log",
            MethodType.methodType(void.class, System.Logger.Level.class, String.class, Throwable.class));
        filter = MethodHandles.lookup().findStatic(PrintFactory.class, "level",
            MethodType.methodType(System.Logger.Level.class, Level.class)); 
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
      mh = MethodHandles.filterArguments(mh, 1, filter);
      MethodHandle target = MethodHandles.permuteArguments(mh,
          MethodType.methodType(void.class, System.Logger.class, String.class, Level.class, Throwable.class),
          new int[] { 0, 2, 1, 3});
      return configClass -> target.bindTo(System.getLogger(configClass.getName()));
    }
    
    private static System.Logger.Level level(Level level) {
      // do not use a switch here, we want this code to be inlined !
      if (level == Level.ERROR) {
        return System.Logger.Level.ERROR;
      }
      if (level == Level.WARNING) {
        return System.Logger.Level.WARNING;
      }
      if (level == Level.INFO) {
        return System.Logger.Level.INFO;
      }
      if (level == Level.DEBUG) {
        return System.Logger.Level.DEBUG;
      }
      if (level == Level.TRACE) {
        return System.Logger.Level.TRACE;
      }
      throw newIllegalStateException();
    }
    
    private static IllegalStateException newIllegalStateException() {
      return new IllegalStateException("unknown level");
    }
  }
  
  
  
  interface ConfigOption {
    ConfigOption enable(boolean enable);
    ConfigOption level(Level level);
    ConfigOption printFactory(PrintFactory factory);
  }
  
  Optional<Boolean> enable();
  Optional<Level> level();
  Optional<PrintFactory> printFactory();
  
  LoggerConfig update(Consumer<? super ConfigOption> configUpdater);
  
  static LoggerConfig fromClass(Class<?> configClass) {
    return fromClass(configClass.getName());
  }
  static LoggerConfig fromClass(String className) {
    return LoggerImpl.configFrom(CLASS, className);
  }
  static LoggerConfig fromPackage(Package packaze) {
    return fromPackage(packaze.getName());
  }
  static LoggerConfig fromPackage(String packageName) {
    return LoggerImpl.configFrom(PACKAGE, packageName);
  }
  static LoggerConfig fromModule(Module module) {
    return fromModule(module.getName());
  }
  static LoggerConfig fromModule(String moduleName) {
    return LoggerImpl.configFrom(MODULE, moduleName);
  }
}

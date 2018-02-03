package com.github.forax.beautifullogger;

import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.CLASS;
import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.MODULE;
import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.PACKAGE;

import java.util.Optional;
import java.util.function.Consumer;

import com.github.forax.beautifullogger.Logger.Level;

public interface LoggerConfig {
  interface Printer {
    void print(String message, Level level, Throwable context);
    
    static Printer system(System.Logger logger) {
      return (message, level, context) -> {
        System.Logger.Level systemLevel = level(level);
        if (context != null) {
          logger.log(systemLevel, message, context);
        } else {
          logger.log(systemLevel, message);
        }
      };
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
    ConfigOption printer(Printer printer);
  }
  
  Optional<Boolean> enable();
  Optional<Level> level();
  Optional<Printer> printer();
  
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

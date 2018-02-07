package com.github.forax.beautifullogger;

import java.lang.StackWalker.Option;
import java.lang.invoke.MethodHandle;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.Supplier;

import com.github.forax.beautifullogger.LoggerConfig.ConfigOption;

public interface Logger {
  public enum Level {
    TRACE, DEBUG, INFO, WARNING, ERROR;
    
    static final Level[] LEVELS = values();
  }
  
  void error(String message, Throwable context);
  void error(Supplier<String> messageProvider);
  <T> void error(Function<? super T, String> messageProvider, T arg0);
  void error(IntFunction<String> messageProvider, int arg0);
  void error(LongFunction<String> messageProvider, long arg0);
  void error(DoubleFunction<String> messageProvider, double arg0);
  <T, U> void error(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1);
  
  void warning(String message, Throwable context);
  void warning(Supplier<String> messageProvider);
  <T> void warning(Function<? super T, String> messageProvider, T arg0);
  void warning(IntFunction<String> messageProvider, int arg0);
  void warning(LongFunction<String> messageProvider, long arg0);
  void warning(DoubleFunction<String> messageProvider, double arg0);
  <T, U> void warning(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1);
  
  void info(String message, Throwable context);
  void info(Supplier<String> messageProvider);
  <T> void info(Function<? super T, String> messageProvider, T arg0);
  void info(IntFunction<String> messageProvider, int arg0);
  void info(LongFunction<String> messageProvider, long arg0);
  void info(DoubleFunction<String> messageProvider, double arg0);
  <T, U> void info(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1);
  
  void debug(String message, Throwable context);
  void debug(Supplier<String> messageProvider);
  <T> void debug(Function<? super T, String> messageProvider, T arg0);
  void debug(IntFunction<String> messageProvider, int arg0);
  void debug(LongFunction<String> messageProvider, long arg0);
  void debug(DoubleFunction<String> messageProvider, double arg0);
  <T, U> void debug(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1);
  
  void trace(String message, Throwable context);
  void trace(Supplier<String> messageProvider);
  <T> void trace(Function<? super T, String> messageProvider, T arg0);
  void trace(IntFunction<String> messageProvider, int arg0);
  void trace(LongFunction<String> messageProvider, long arg0);
  void trace(DoubleFunction<String> messageProvider, double arg0);
  <T, U> void trace(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1);
  
  public static Logger getLogger() {
    Class<?> declaringClass = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
    return getLogger(declaringClass, LoggerImpl.EMPTY_CONSUMER);
  }
  
  public static Logger getLogger(Class<?> configClass) {
    return getLogger(configClass, LoggerImpl.EMPTY_CONSUMER);
  }
  
  public static Logger getLogger(Class<?> configClass, Consumer<? super ConfigOption> configInitializer) {
    if (configInitializer != LoggerImpl.EMPTY_CONSUMER) {
      LoggerConfig.fromClass(configClass).update(configInitializer);
    }
    MethodHandle mh = LoggerImpl.getLoggingMethodHandle(configClass, 4); 
    return LoggerImpl.createLogger(mh);
  }
}

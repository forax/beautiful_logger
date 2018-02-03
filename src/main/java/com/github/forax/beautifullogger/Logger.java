package com.github.forax.beautifullogger;

import static com.github.forax.beautifullogger.LoggerImpl.NONE;

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
  
  public void log(Level level, Throwable context, Object messageProvider, Object arg0, Object arg1, Object arg2, Object arg3);
  
  public default void error(String message, Throwable context) {
    log(Level.ERROR, context, message, NONE, NONE, NONE, NONE);
  }
  public default void error(Supplier<String> messageProvider) {
    log(Level.ERROR, null, messageProvider, NONE, NONE, NONE, NONE);
  }
  public default <T> void error(Function<? super T, String> messageProvider, T arg0) {
    log(Level.ERROR, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void error(IntFunction<String> messageProvider, int arg0) {
    log(Level.ERROR, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void error(LongFunction<String> messageProvider, long arg0) {
    log(Level.ERROR, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void error(DoubleFunction<String> messageProvider, double arg0) {
    log(Level.ERROR, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default <T, U> void error(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1) {
    log(Level.ERROR, null, messageProvider, arg0, arg1, NONE, NONE);
  }
  
  public default void warning(String message, Throwable context) {
    log(Level.WARNING, context, message, NONE, NONE, NONE, NONE);
  }
  public default void warning(Supplier<String> messageProvider) {
    log(Level.WARNING, null, messageProvider, NONE, NONE, NONE, NONE);
  }
  public default <T> void warning(Function<? super T, String> messageProvider, T arg0) {
    log(Level.WARNING, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void warning(IntFunction<String> messageProvider, int arg0) {
    log(Level.WARNING, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void warning(LongFunction<String> messageProvider, long arg0) {
    log(Level.WARNING, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void warning(DoubleFunction<String> messageProvider, double arg0) {
    log(Level.WARNING, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default <T, U> void warning(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1) {
    log(Level.WARNING, null, messageProvider, arg0, arg1, NONE, NONE);
  }
  
  public default void info(String message, Throwable context) {
    log(Level.INFO, context, message, NONE, NONE, NONE, NONE);
  }
  public default void info(Supplier<String> messageProvider) {
    log(Level.INFO, null, messageProvider, NONE, NONE, NONE, NONE);
  }
  public default <T> void info(Function<? super T, String> messageProvider, T arg0) {
    log(Level.INFO, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void info(IntFunction<String> messageProvider, int arg0) {
    log(Level.INFO, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void info(LongFunction<String> messageProvider, long arg0) {
    log(Level.INFO, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void info(DoubleFunction<String> messageProvider, double arg0) {
    log(Level.INFO, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default <T, U> void info(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1) {
    log(Level.INFO, null, messageProvider, arg0, arg1, NONE, NONE);
  }
  
  public default void debug(String message, Throwable context) {
    log(Level.DEBUG, context, message, NONE, NONE, NONE, NONE);
  }
  public default void debug(Supplier<String> messageProvider) {
    log(Level.DEBUG, null, messageProvider, NONE, NONE, NONE, NONE);
  }
  public default <T> void debug(Function<? super T, String> messageProvider, T arg0) {
    log(Level.DEBUG, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void debug(IntFunction<String> messageProvider, int arg0) {
    log(Level.DEBUG, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void debug(LongFunction<String> messageProvider, long arg0) {
    log(Level.DEBUG, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void debug(DoubleFunction<String> messageProvider, double arg0) {
    log(Level.DEBUG, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default <T, U> void debug(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1) {
    log(Level.DEBUG, null, messageProvider, arg0, arg1, NONE, NONE);
  }
  
  public default void trace(String message, Throwable context) {
    log(Level.TRACE, context, message, NONE, NONE, NONE, NONE);
  }
  public default void trace(Supplier<String> messageProvider) {
    log(Level.TRACE, null, messageProvider, NONE, NONE, NONE, NONE);
  }
  public default <T> void trace(Function<? super T, String> messageProvider, T arg0) {
    log(Level.TRACE, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void trace(IntFunction<String> messageProvider, int arg0) {
    log(Level.TRACE, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void trace(LongFunction<String> messageProvider, long arg0) {
    log(Level.TRACE, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default void trace(DoubleFunction<String> messageProvider, double arg0) {
    log(Level.TRACE, null, messageProvider, arg0, NONE, NONE, NONE);
  }
  public default <T, U> void trace(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1) {
    log(Level.TRACE, null, messageProvider, arg0, arg1, NONE, NONE);
  }
  
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
    return (level, context, messageProvider, arg0, arg1, arg2, arg3) -> {
      try {
        mh.invokeExact(level, context, messageProvider, arg0, arg1, arg2, arg3);
      } catch(Throwable e) {
        throw LoggerImpl.rethrow(e);
      }
    };
  }
}

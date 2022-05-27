package com.github.forax.beautifullogger.perf;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import java.io.PrintStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@SuppressWarnings("static-method")
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class LoggerDisabledBenchMark {
  static class LevelLogger {
    enum Level {
      WARNING, DEBUG
    }
     
    final Level level;
    
    public LevelLogger(Level level) {
      this.level = level;
    }
    
    public boolean isDebugEnabled() {
      return level.compareTo(Level.DEBUG) >= 0;
    }
    
    public void debug(String message) {
      if (isDebugEnabled()) {
        System.out.println(message);
      }
    }
  }

  interface Configuration {
    public Object getFilter();
  }
  static class ConfigurationImpl implements Configuration {
    private final Object filter;

    ConfigurationImpl(Object filter) { this.filter = filter; }
    
    @Override
    public Object getFilter() {
      return filter;
    }
  }
  
  interface LambdaLogger {
    enum Level {
      WARNING, DEBUG, TRACE;
    }
    
    default void debug(String message) {
      log(Level.DEBUG, message);
    }
    void log(Level level, String messsage);
    
    public static LambdaLogger getLogger(Class<?> configClass) {
      MethodHandle mh = getLoggingMethodHandle(configClass); 
      return (level, message) -> {
        try {
          mh.invokeExact(level, message);
        } catch (Throwable e) {
          throw new AssertionError(e);
        }
      };
      /*return new LambdaLogger() {
        @Override
        public void log(Level level, String message) {
          try {
            mh.invokeExact(level, message);
          } catch (Throwable e) {
            throw new AssertionError(e);
          }
        }
      };*/
    }
    
    
    @SuppressWarnings("unused")
    static boolean isWarning(Level l) {
      return l == Level.WARNING;
    }

    @SuppressWarnings("unused")
    static void empty(Level level, String message) {
      // do nothing
    }

    public static MethodHandle getLoggingMethodHandle(@SuppressWarnings("unused") Class<?> configClass) {
      Lookup lookup = lookup();
      MethodHandle mh;
      try {
        mh = lookup.findVirtual(PrintStream.class, "println", methodType(void.class, String.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
      mh = mh.bindTo(System.out);
      mh = MethodHandles.dropArguments(mh, 0, Level.class);
      
      MethodHandle test;
      try {
        test = lookup.findStatic(LambdaLogger.class, "isWarning", methodType(boolean.class, Level.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
      MethodHandle empty;
      try {
        empty = lookup.findStatic(LambdaLogger.class, "empty", methodType(void.class, Level.class, String.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
      return MethodHandles.guardWithTest(test, mh, empty);
    }
  }

  private static final org.apache.logging.log4j.Logger LOG4J2_LOGGER =
      org.apache.logging.log4j.LogManager.getLogger(LoggerDisabledBenchMark.class);
  private static final org.slf4j.Logger LOGBACK_LOGGER =
      org.slf4j.LoggerFactory.getLogger(LoggerDisabledLoopBenchMark.class);
  private static final java.util.logging.Logger JUL_LOGGER =
      java.util.logging.Logger.getLogger(LoggerDisabledLoopBenchMark.class.getName());
  private static final com.google.common.flogger.FluentLogger FLUENT_LOGGER =
      com.google.common.flogger.FluentLogger.forEnclosingClass();
  private static final LevelLogger LEVEL_LOGGER =
      new LevelLogger(LevelLogger.Level.WARNING);
  private static final LambdaLogger LAMBDA_LOGGER =
      LambdaLogger.getLogger(LoggerDisabledBenchMark.class);
  private static final com.github.forax.beautifullogger.Logger BEAUTIFUL_LOGGER =
      com.github.forax.beautifullogger.Logger.getLogger();

  @Benchmark
  public void no_op() {
    // empty
  }
  
  @Benchmark
  public void level_disabled() {
    LEVEL_LOGGER.debug("should not be printed !");
  }
  
  @Benchmark
  public void lambda_disabled() {
    LAMBDA_LOGGER.debug("should not be printed !");
  }

  //FIXME
//  @Benchmark
//  public void log4j2_disabled_text() {
//    LOG4J2_LOGGER.debug("should not be printed !");
//  }
//
//  @Benchmark
//  public void log4j2_disabled_block() {
//    if (LOG4J2_LOGGER.isDebugEnabled()) {
//      LOG4J2_LOGGER.debug("should not be printed !");
//    }
//  }
//
//  @Benchmark
//  public void log4j2_disable_lambda() {
//    LOG4J2_LOGGER.debug(() -> "should not be printed !");
//  }
  
  @Benchmark
  public void logback_disable_lambda() {
    LOGBACK_LOGGER.debug("should not be printed !");
  }

  @Benchmark
  public void jul_disable_lambda() {
    JUL_LOGGER.fine("should not be printed !");
  }
  
  @Benchmark
  public void flogger_disable() {
    FLUENT_LOGGER.atFine().log("should not be printed !");
  }
  
  @Benchmark
  public void beautiful_logger_disabled() {
    BEAUTIFUL_LOGGER.debug(() -> "should not be printed !");
  }
  
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(LoggerDisabledBenchMark.class.getName()).build();
    new Runner(opt).run();
  }
}

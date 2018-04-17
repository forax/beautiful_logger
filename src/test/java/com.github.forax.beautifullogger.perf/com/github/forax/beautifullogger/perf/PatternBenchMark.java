package com.github.forax.beautifullogger.perf;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/*@SuppressWarnings("static-method")
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)*/
/*public class PatternBenchMark {
  static class SimpleLogger {
    enum Level {
      WARNING, DEBUG
    }
    
    private static final int DEBUG_LEVEL = Level.DEBUG.ordinal();
     
    final int level;
    
    public SimpleLogger(Level level) {
      this.level = level.ordinal();
    }
    
    public boolean isDebugEnabled() {
      return level >= DEBUG_LEVEL;
    }
    
    public void debug(String message) {
      if (isDebugEnabled()) {
        System.out.println(message);
      }
    }
    
    public void debug(String format, Object... args) {
      if (isDebugEnabled()) {
        System.out.println(String.format(format, args));
      }
    }
    
    public void debug(Supplier<String> supplier) {
      if (isDebugEnabled()) {
        System.out.println(supplier.get());
      }
    }
    
    public void debug(int value, IntFunction<String> fun) {
      if (isDebugEnabled()) {
        System.out.println(fun.apply(value));
      }
    }
  }
  
  private static final SimpleLogger LOGGER =
      new SimpleLogger(SimpleLogger.Level.WARNING);
  
  private static final org.apache.logging.log4j.Logger LOGGER =
      org.apache.logging.log4j.LogManager.getLogger(LoggerDisabledBenchMark.class);
  
  private int value;
  
  @Setup
  public void setup() {
    value = new Random(0).nextInt();
  }
  
  @Benchmark
  public void logger_concat() {
    LOGGER.debug("processor count " + value);
  }
  
  @Benchmark
  public void logger_builder() {
    LOGGER.debug(new StringBuilder().append("processor count ").append(value).toString());
  }
  
  @Benchmark
  public void logger_lambda_supplier() {
    LOGGER.debug(() -> "processor count " + value);
  }
  
  @Benchmark
  public void logger_inner_class_supplier() {
    int value = this.value;
    LOGGER.debug(new Supplier<String>() {
      @Override
      public String get() {
        return "processor count " + value;
      }
    });
  }
  
  @Benchmark
  public void logger_lambda_function() {
    LOGGER.debug(value, _value -> "processor count " + _value);
  }
  
  @Benchmark
  public void logger_format() {
    LOGGER.debug("processor count %d", new Object[] { value });
  }
  
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(PatternBenchMark.class.getName()).build();
    new Runner(opt).run();
  }
}*/

package com.github.forax.beautifullogger.perf;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class LoggerDisabledLoopBenchMark {
  private static final org.apache.logging.log4j.Logger LOG4J2_LOGGER =
      org.apache.logging.log4j.LogManager.getLogger(LoggerDisabledLoopBenchMark.class);
  private static final org.slf4j.Logger LOGBACK_LOGGER =
      org.slf4j.LoggerFactory.getLogger(LoggerDisabledLoopBenchMark.class);
  private static final java.util.logging.Logger JUL_LOGGER =
      java.util.logging.Logger.getLogger(LoggerDisabledLoopBenchMark.class.getName());
  private static final com.github.forax.beautifullogger.Logger BEAUTIFUL_LOGGER =
      com.github.forax.beautifullogger.Logger.getLogger();
  
  static final int ARRAY_SIZE = 1 << 20;
  final int[] array = new int[ARRAY_SIZE];

  @Setup
  public void setup() {
    Random random = new Random(0);
    Arrays.setAll(array, i-> random.nextInt());
  }
  
  @Benchmark
  public int empty_sum() {
    int sum = 0;
    for(int i = 0; i < array.length; i++) {
      sum += array[i];
    }
    return sum;
  }
  
  @Benchmark
  public int log4j2_sum() {
    int sum = 0;
    for(int i = 0; i < array.length; i++) {
      sum += array[i];
      LOG4J2_LOGGER.debug("should not be printed !");
    }
    return sum;
  }
  
  @Benchmark
  public int log4j2_foreach_sum() {
    int sum = 0;
    for(int value: array) {
      sum += value;
      LOG4J2_LOGGER.debug("should not be printed !");
    }
    return sum;
  }
  
  @Benchmark
  public int logback_sum() {
    int sum = 0;
    for(int i = 0; i < array.length; i++) {
      sum += array[i];
      LOGBACK_LOGGER.debug("should not be printed !");
    }
    return sum;
  }
  
  @Benchmark
  public int jul_foreach_sum() {
    int sum = 0;
    for(int value: array) {
      sum += value;
      JUL_LOGGER.fine("should not be printed !");
    }
    return sum;
  }
  
  @Benchmark
  public int jul_sum() {
    int sum = 0;
    for(int i = 0; i < array.length; i++) {
      sum += array[i];
      JUL_LOGGER.fine("should not be printed !");
    }
    return sum;
  }
  
  @Benchmark
  public int logback_foreach_sum() {
    int sum = 0;
    for(int value: array) {
      sum += value;
      LOGBACK_LOGGER.debug("should not be printed !");
    }
    return sum;
  }
  
  @Benchmark
  public int beautiful_loggger_sum() {
    int sum = 0;
    for(int i = 0; i < array.length; i++) {
      sum += array[i];
      BEAUTIFUL_LOGGER.debug(() -> "should not be printed !");
    }
    return sum;
  }
  
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(LoggerDisabledLoopBenchMark.class.getName()).build();
    new Runner(opt).run();
  }
}

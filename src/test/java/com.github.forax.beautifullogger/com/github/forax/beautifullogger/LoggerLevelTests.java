package com.github.forax.beautifullogger;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.forax.beautifullogger.Logger;
import com.github.forax.beautifullogger.Logger.Level;

@SuppressWarnings("static-method")
class LoggerLevelTests {

  private static Stream<Arguments> logNullThrowableAndLevelPairSource() {
    List<Entry<Consumer<Logger>, Level>> list = List.of(
        entry(l -> l.debug("hello", null),   Level.DEBUG),
        entry(l -> l.error("hello", null),   Level.ERROR),
        entry(l -> l.info("hello", null),    Level.INFO),
        entry(l -> l.trace("hello", null),   Level.TRACE),
        entry(l -> l.warning("hello", null), Level.WARNING));
    return list.stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
  }

  @ParameterizedTest
  @MethodSource("logNullThrowableAndLevelPairSource")
  void logNullThrowableAndLevelMatch(Consumer<Logger> consumer, Level level) {
    boolean[] marked = { false };
    Logger logger = Logger.getLogger(
        new Object() {/*empty*/}.getClass(),
        opt -> opt.level(Level.TRACE).printer((message, loggerLevel, context) -> {
          marked[0] = true;
          assertAll(
            () -> assertEquals(level, loggerLevel),
            () -> assertEquals("hello", message),
            () -> assertNull(context));
        }));
    consumer.accept(logger); 
    assertTrue(marked[0]);
  }
  
  private static Stream<Arguments> logThrowableAndLevelPairSource() {
    List<Entry<BiConsumer<Logger, Throwable>, Level>> list = List.of(
        entry((l, t) -> l.debug("exception", t),   Level.DEBUG),
        entry((l, t) -> l.error("exception", t),   Level.ERROR),
        entry((l, t) -> l.info("exception", t),    Level.INFO),
        entry((l, t) -> l.trace("exception", t),   Level.TRACE),
        entry((l, t) -> l.warning("exception", t), Level.WARNING));
    return list.stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
  }
  @ParameterizedTest
  @MethodSource("logThrowableAndLevelPairSource")
  void logThrowableAndLevelMatch(BiConsumer<Logger, Throwable> consumer, Level level) {
    Throwable throwable = new Throwable();
    boolean[] marked = { false };
    Logger logger = Logger.getLogger(
        new Object() {/*empty*/}.getClass(),
        opt -> opt.level(Level.TRACE).printer((message, loggerLevel, context) -> {
          marked[0] = true;
          assertAll(
            () -> assertEquals(level, loggerLevel),
            () -> assertEquals("exception", message),
            () -> assertSame(throwable, context));
        }));
    consumer.accept(logger, throwable);
    assertTrue(marked[0]);
  }
  
  private static Stream<Arguments> logSupplierAndLevelPairSource() {
    List<Entry<BiConsumer<Logger, Supplier<String>>, Level>> list = List.of(
        entry(Logger::debug,   Level.DEBUG),
        entry(Logger::error,   Level.ERROR),
        entry(Logger::info,    Level.INFO),
        entry(Logger::trace,   Level.TRACE),
        entry(Logger::warning, Level.WARNING));
    return list.stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
  }
  @ParameterizedTest
  @MethodSource("logSupplierAndLevelPairSource")
  void logSupplierAndLevelMatch(BiConsumer<Logger, Supplier<String>> consumer, Level level) {
    boolean[] marked = { false };
    Logger logger = Logger.getLogger(
        new Object() {/*empty*/}.getClass(),
        opt -> opt.level(Level.TRACE).printer((message, loggerLevel, context) -> {
          marked[0] = true;
          assertAll(
            () -> assertEquals(level, loggerLevel),
            () -> assertEquals("foo", message),
            () -> assertNull(context));
        }));
    consumer.accept(logger, () -> "foo"); 
    assertTrue(marked[0]);
  }
  
  static Stream<Arguments> logIntFunctionAndLevelPairSource() {
    List<Entry<Consumer<Logger>, Level>> list = List.of(
        entry(l -> l.debug((int v) -> "" + v, 1),   Level.DEBUG),
        entry(l -> l.error((int v) -> "" + v, 1),   Level.ERROR),
        entry(l -> l.info((int v) -> "" + v, 1),    Level.INFO),
        entry(l -> l.trace((int v) -> "" + v, 1),   Level.TRACE),
        entry(l -> l.warning((int v) -> "" + v, 1), Level.WARNING));
    return list.stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
  }
  @ParameterizedTest
  @MethodSource("logIntFunctionAndLevelPairSource")
  void logIntFunctionAndLevelMatch(Consumer<Logger> consumer, Level level) {
    boolean[] marked = { false };
    Logger logger = Logger.getLogger(
        new Object() {/*empty*/}.getClass(),
        opt -> opt.level(Level.TRACE).printer((message, loggerLevel, context) -> {
          marked[0] = true;
          assertAll(
            () -> assertEquals(level, loggerLevel),
            () -> assertEquals("1", message),
            () -> assertNull(context));
        }));
    consumer.accept(logger);
    assertTrue(marked[0]);
  }


  static Stream<Arguments> logLongFunctionAndLevelPairSource() {
    List<Entry<Consumer<Logger>, Level>> list = List.of(
        entry(l -> l.debug((long v) -> "" + v, 2L),   Level.DEBUG),
        entry(l -> l.error((long v) -> "" + v, 2L),   Level.ERROR),
        entry(l -> l.info((long v) -> "" + v, 2L),    Level.INFO),
        entry(l -> l.trace((long v) -> "" + v, 2L),   Level.TRACE),
        entry(l -> l.warning((long v) -> "" + v, 2L), Level.WARNING));
    return list.stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
  }
  @ParameterizedTest
  @MethodSource("logLongFunctionAndLevelPairSource")
  void logLongFunctionAndLevelMatch(Consumer<Logger> consumer, Level level) {
    boolean[] marked = { false };
    Logger logger = Logger.getLogger(
        new Object() {/*empty*/}.getClass(),
        opt -> opt.level(Level.TRACE).printer((message, loggerLevel, context) -> {
          marked[0] = true;
          assertAll(
            () -> assertEquals(level, loggerLevel),
            () -> assertEquals("2", message),
            () -> assertNull(context));
        }));
    consumer.accept(logger);
    assertTrue(marked[0]);
  }

  private static Stream<Arguments> logDoubleFunctionAndLevelPairSource() {
    List<Entry<Consumer<Logger>, Level>> list = List.of(
        entry(l -> l.debug(Double::toString, 3.0),   Level.DEBUG),
        entry(l -> l.error(Double::toString, 3.0),   Level.ERROR),
        entry(l -> l.info(Double::toString, 3.0),    Level.INFO),
        entry(l -> l.trace(Double::toString, 3.0),   Level.TRACE),
        entry(l -> l.warning(Double::toString, 3.0), Level.WARNING));
    return list.stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
  }
  @ParameterizedTest
  @MethodSource("logDoubleFunctionAndLevelPairSource")
  void logDoubleFunctionAndLevelMatch(Consumer<Logger> consumer, Level level) {
    boolean[] marked = { false };
    Logger logger = Logger.getLogger(
        new Object() {/*empty*/}.getClass(),
        opt -> opt.level(Level.TRACE).printer((message, loggerLevel, context) -> {
          marked[0] = true;
          assertAll(
            () -> assertEquals(level, loggerLevel),
            () -> assertEquals("3.0", message),
            () -> assertNull(context));
        }));
    consumer.accept(logger);
    assertTrue(marked[0]);
  }
  
  private static Stream<Arguments> logFunctionAndLevelPairSource() {
    List<Entry<Consumer<Logger>, Level>> list = List.of(
        entry(l -> l.debug(x -> x, "bar"),   Level.DEBUG),
        entry(l -> l.error(x -> x, "bar"),   Level.ERROR),
        entry(l -> l.info(x -> x, "bar"),    Level.INFO),
        entry(l -> l.trace(x -> x, "bar"),   Level.TRACE),
        entry(l -> l.warning(x -> x, "bar"), Level.WARNING));
    return list.stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
  }
  @ParameterizedTest
  @MethodSource("logFunctionAndLevelPairSource")
  void logFunctionAndLevelMatch(Consumer<Logger> consumer, Level level) {
    boolean[] marked = { false };
    Logger logger = Logger.getLogger(
        new Object() {/*empty*/}.getClass(),
        opt -> opt.level(Level.TRACE).printer((message, loggerLevel, context) -> {
          marked[0] = true;
          assertAll(
            () -> assertEquals(level, loggerLevel),
            () -> assertEquals("bar", message),
            () -> assertNull(context));
        }));
    consumer.accept(logger); 
    assertTrue(marked[0]);
  }
  
  private static Stream<Arguments> logBiFunctionAndLevelPairSource() {
    List<Entry<Consumer<Logger>, Level>> list = List.of(
        entry(l -> l.debug((a, b) -> a + b, "foo", "bar"),   Level.DEBUG),
        entry(l -> l.error((a, b) -> a + b, "foo", "bar"),   Level.ERROR),
        entry(l -> l.info((a, b) -> a + b, "foo", "bar"),    Level.INFO),
        entry(l -> l.trace((a, b) -> a + b, "foo", "bar"),   Level.TRACE),
        entry(l -> l.warning((a, b) -> a + b, "foo", "bar"), Level.WARNING));
    return list.stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
  }
  @ParameterizedTest
  @MethodSource("logBiFunctionAndLevelPairSource")
  void logBiFunctionAndLevelMatch(Consumer<Logger> consumer, Level level) {
    boolean[] marked = { false };
    Logger logger = Logger.getLogger(
        new Object() {/*empty*/}.getClass(),
        opt -> opt.level(Level.TRACE).printer((message, loggerLevel, context) -> {
          marked[0] = true;
          assertAll(
            () -> assertEquals(level, loggerLevel),
            () -> assertEquals("foobar", message),
            () -> assertNull(context));
        }));
    consumer.accept(logger); 
    assertTrue(marked[0]);
  }
}

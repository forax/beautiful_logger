package com.github.forax.beautifullogger;

import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.Supplier;

import com.github.forax.beautifullogger.LoggerConfig.ConfigOption;

/**
 * Log events of the application.
 * 
 * A logger is create using one of the methods {@link #getLogger(Class, Consumer) getLogger}.
 * The {@link LoggerConfig configuration} of a that Logger is associated to the class
 * that construct it.
 * 
 * <pre>
 * import com.github.forax.beautifullogger.Logger;
 *
 * class Example {
 *   private static final Logger LOGGER = Logger.getLogger();
 *   
 *   ...
 * </pre>
 * 
 * To log an event, one should call the method named by the severity of the event,
 * trace for level TRACE, debug for level DEBUG, etc.
 * 
 * <pre>
 *   public static void main(String[] args) {
 *     LOGGER.info((int length) -&gt; "there are " + length + " arguments", args.length);
 *   
 *     ...
 * </pre>
 * 
 * Each logging method has several overloads allowing to choose a message provider
 * to avoid if possible, allocations due to concatenation, non constant lambda or
 * boxing of primitive values.
 * 
 * <p>&nbsp;</p>
 * <table>
 *  <tr><th>message providers</th></tr>
 *  <tr><td>String</td>        <td>constant String (do not use concatenation !)</td></tr>
 *  <tr><td>Supplier</td>      <td>costly operation that does not need arguments</td></tr>
 *  <tr><td>Function</td>      <td>costly operation that needs one Object argument</td></tr>
 *  <tr><td>IntFunction</td>   <td>costly operation that needs one integer argument</td></tr>
 *  <tr><td>LongFunction</td>  <td>costly operation that needs one long argument</td></tr>
 *  <tr><td>DoubleFunction</td><td>costly operation that needs one double argument</td></tr>
 *  <tr><td>BiFunction</td>    <td>costly operation that needs two arguments</td></tr>
 *  <caption>supported message providers</caption>
 * </table>
 * <p>&nbsp;</p>
 * 
 * For other signatures, you can also create your own {@link LogService}.
 */
public interface Logger {
  /**
   * Log levels used for indicating the severity of an event. 
   * 
   * Log levels are ordered from the least specific to the most,
   * TRACE &lt; DEBUG &lt; INFO &lt; WARNING &lt; ERROR.
   *
   * All methods of {@link Logger} are emitted with a a log level
   * corresponding to name of the method, the configuration level
   * of a Logger can be changed using {@link LoggerConfig#update(Consumer) logConfig.update(upd -> upd.level(aLevel))}.
   * 
   * @see LoggerConfig#level()
   */
  public enum Level {
    /** a fine grained debug level */
    TRACE,
    /** a general debug level */
    DEBUG,
    /** an information level */
    INFO,
    /** a non normal state level */
    WARNING,
    /** an error level */
    ERROR;
    
    static final Level[] LEVELS = values();
  }
  
  /**
   * Log a message at {@link Level#ERROR} level with an exception.
   * @param message the log message.
   * @param context an exception or null.
   */
  void error(String message, Throwable context);
  /**
   * Log a message provided by the supplier at {@link Level#ERROR} level.
   * @param messageProvider the provider of the message.
   */
  void error(Supplier<String> messageProvider);
  /**
   * Log a message provided by the function at {@link Level#ERROR} level.
   * @param <T> the type of the first argument.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  <T> void error(Function<? super T, String> messageProvider, T arg0);
  /**
   * Log a message provided by the function at {@link Level#ERROR} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void error(IntFunction<String> messageProvider, int arg0);
  /**
   * Log a message provided by the function at {@link Level#ERROR} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void error(LongFunction<String> messageProvider, long arg0);
  /**
   * Log a message provided by the function at {@link Level#ERROR} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void error(DoubleFunction<String> messageProvider, double arg0);
  /**
   * Log a message provided by the function with two arguments at {@link Level#ERROR} level.
   * @param <T> the type of the first argument.
   * @param <U> the type of the second argument.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   * @param arg1 the second argument.
   */
  <T, U> void error(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1);
  
  /**
   * Log a message at {@link Level#WARNING} level with an exception.
   * @param message the log message.
   * @param context an exception or null.
   */
  void warning(String message, Throwable context);
  /**
   * Log a message provided by the supplier at {@link Level#WARNING} level.
   * @param messageProvider the provider of the message.
   */
  void warning(Supplier<String> messageProvider);
  /**
   * Log a message provided by the function at {@link Level#WARNING} level.
   * @param <T> the type of the first argument.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  <T> void warning(Function<? super T, String> messageProvider, T arg0);
  /**
   * Log a message provided by the function at {@link Level#WARNING} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void warning(IntFunction<String> messageProvider, int arg0);
  /**
   * Log a message provided by the function at {@link Level#WARNING} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void warning(LongFunction<String> messageProvider, long arg0);
  /**
   * Log a message provided by the function at {@link Level#WARNING} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void warning(DoubleFunction<String> messageProvider, double arg0);
  /**
   * Log a message provided by the function with two arguments at {@link Level#WARNING} level.
   * @param <T> the type of the first argument.
   * @param <U> the type of the second argument.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   * @param arg1 the second argument.
   */
  <T, U> void warning(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1);
  
  /**
   * Log a message at {@link Level#INFO} level with an exception.
   * @param message the log message.
   * @param context an exception or null.
   */
  void info(String message, Throwable context);
  /**
   * Log a message provided by the supplier at {@link Level#INFO} level.
   * @param messageProvider the provider of the message.
   */
  void info(Supplier<String> messageProvider);
  /**
   * Log a message provided by the function at {@link Level#INFO} level.
   * @param <T> the type of the first argument.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  <T> void info(Function<? super T, String> messageProvider, T arg0);
  /**
   * Log a message provided by the function at {@link Level#INFO} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void info(IntFunction<String> messageProvider, int arg0);
  /**
   * Log a message provided by the function at {@link Level#INFO} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void info(LongFunction<String> messageProvider, long arg0);
  /**
   * Log a message provided by the function at {@link Level#INFO} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void info(DoubleFunction<String> messageProvider, double arg0);
  /**
   * Log a message provided by the function with two arguments at {@link Level#INFO} level.
   * @param <T> the type of the first argument.
   * @param <U> the type of the second argument.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   * @param arg1 the second argument.
   */
  <T, U> void info(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1);
  
  /**
   * Log a message at {@link Level#DEBUG} level with an exception.
   * @param message the log message.
   * @param context an exception or null.
   */
  void debug(String message, Throwable context);
  /**
   * Log a message provided by the supplier at {@link Level#DEBUG} level.
   * @param messageProvider the provider of the message.
   */
  void debug(Supplier<String> messageProvider);
  /**
   * Log a message provided by the function at {@link Level#DEBUG} level.
   * @param <T> the type of the first argument.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  <T> void debug(Function<? super T, String> messageProvider, T arg0);
  /**
   * Log a message provided by the function at {@link Level#DEBUG} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void debug(IntFunction<String> messageProvider, int arg0);
  /**
   * Log a message provided by the function at {@link Level#DEBUG} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void debug(LongFunction<String> messageProvider, long arg0);
  /**
   * Log a message provided by the function at {@link Level#DEBUG} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void debug(DoubleFunction<String> messageProvider, double arg0);
  /**
   * Log a message provided by the function with two arguments at {@link Level#DEBUG} level.
   * @param <T> the type of the first argument.
   * @param <U> the type of the second argument.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   * @param arg1 the second argument.
   */
  <T, U> void debug(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1);
  
  /**
   * Log a message at {@link Level#TRACE} level with an exception.
   * @param message the log message.
   * @param context an exception or null.
   */
  void trace(String message, Throwable context);
  /**
   * Log a message provided by the supplier at {@link Level#TRACE} level.
   * @param messageProvider the provider of the message.
   */
  void trace(Supplier<String> messageProvider);
  /**
   * Log a message provided by the function at {@link Level#TRACE} level.
   * @param <T> the type of the first argument.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  <T> void trace(Function<? super T, String> messageProvider, T arg0);
  /**
   * Log a message provided by the function at {@link Level#TRACE} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void trace(IntFunction<String> messageProvider, int arg0);
  /**
   * Log a message provided by the function at {@link Level#TRACE} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void trace(LongFunction<String> messageProvider, long arg0);
  /**
   * Log a message provided by the function at {@link Level#TRACE} level.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   */
  void trace(DoubleFunction<String> messageProvider, double arg0);
  /**
   * Log a message provided by the function with two arguments at {@link Level#TRACE} level.
   * @param <T> the type of the first argument.
   * @param <U> the type of the second argument.
   * @param messageProvider the provider of the message.
   * @param arg0 the first argument.
   * @param arg1 the second argument.
   */
  <T, U> void trace(BiFunction<? super T, ? super U, String> messageProvider, T arg0, U arg1);
  
  /**
   * Create a logger with the configuration of class that calls this method.
   * 
   * @return a logger.
   * 
   * @see LoggerConfig#fromClass(Class)
   */
  public static Logger getLogger() {
    Class<?> declaringClass = LoggerImpl.IS_JAVA_8?
        LoggerImpl.GetCallerHolder.getCallerClass():
        LoggerImpl.StackWalkerHolder.STACK_WALKER.getCallerClass();
    return getLogger(declaringClass, LoggerImpl.EMPTY_CONSUMER);
  }
  
  /**
   * Create a logger with the configuration of the configClass.
   * 
   * @param configClass the class that hold the configuration.
   * @return a logger.
   * @throws NullPointerException if the configuration class is null.
   * 
   * @see LoggerConfig#fromClass(Class)
   */
  public static Logger getLogger(Class<?> configClass) {
    return getLogger(configClass, LoggerImpl.EMPTY_CONSUMER);
  }
  
  /**
   * Create a logger with the configuration of the configClass and an initializer to change
   * the configuration.
   * 
   * @param configClass the class that hold the configuration.
   * @param configUpdater a consumer that can update a configuration.
   * @return a logger.
   * @throws NullPointerException if the configuration class or the configuration updater is null.
   * 
   * @see LoggerConfig#fromClass(Class)
   */
  public static Logger getLogger(Class<?> configClass, Consumer<? super ConfigOption> configUpdater) {
    Objects.requireNonNull(configClass);
    Objects.requireNonNull(configUpdater);
    if (configUpdater != LoggerImpl.EMPTY_CONSUMER) {
      LoggerConfig.fromClass(configClass).update(configUpdater);
    }
    MethodHandle mh = LoggerImpl.getLoggingMethodHandle(configClass, 4); 
    return LoggerImpl.createLogger(mh);
  }
}

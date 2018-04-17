package com.github.forax.beautifullogger;

import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.CLASS;
import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.MODULE;
import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.PACKAGE;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.github.forax.beautifullogger.Logger.Level;

/**
 * The configuration of a {@link Logger}.
 * 
 * The configuration can be set on a {@link LoggerConfig#fromModule(String) module},
 * a {@link LoggerConfig#fromPackage(Package) package} or
 * a {@link LoggerConfig#fromClass(Class) class}.
 * When a logger read a configuration property, it first tries to read the configuration property
 * of its configuration class, if the property is not defined, it tries to read the configuration property of the package
 * and if it is not defined, it tries to read the configuration property of the module, if it is not defined,
 * it uses the default values below.
 * 
 * <p>&nbsp;</p>
 * <table>
 *  <tr><td>enable</td><td>true</td></tr>
 *  <tr><td>level</td><td>{@link Level#INFO}</td></tr>
 *  <tr><td>logEventFactory</td><td>{@link LogEventFactory#defaultFactory()}</td></tr>
 *  <caption>default value of the configuration properties</caption>
 * </table>
 * <p>&nbsp;</p>
 * 
 * An example of reading the configuration
 * <pre>
 * class Example {
 *   private static final Logger LOGGER = Logger.getLogger(Example.class);
 *   
 *   public static void main(String[] args) {
 *     LogerConfig config = LoggerConfig.fromClass(Example.class);
 *     System.out.println("logger enabled" + config.enable());
 *     ...
 * </pre>
 * 
 * Because changing a configuration property may cause the Java Virtual Machine to de-optimize assembly codes,
 * the change to the properties of a LoggerConfig has to be done in bulk using the method {@link LoggerConfig#update(Consumer)}.
 * 
 * By example to change the level of a configuration
 * <pre>
 *    config.update(opt -&gt; opt.level(Level.TRACE)); 
 * </pre>
 * 
 */
public interface LoggerConfig {
  /**
   * The interface that provides a method handle that can emit
   * a logging event for a configuration class.
   * 
   * @see LoggerConfig#logEventFactory()
   * @see ConfigOption#logEventFactory(LogEventFactory)
   */
  @FunctionalInterface
  interface LogEventFactory {
    /**
     * Returns a method handle that can emit a logging event for a configuration class, its method type must be
     * {@link java.lang.invoke.MethodType#methodType(Class, Class[]) MethodType#methodType(void.class, String.class, Level.class, Throwable.class)}.
     * This method is not called for each event but more or less each time
     * the runtime detects that the configuration has changed.
     * 
     * @param configClass the configuration class.
     * @return a method handle that can emit a logging event.
     */
    MethodHandle getPrintMethodHandle(Class<?> configClass);
    
    /**
     * A strategy represent a Logger factory that may or may not be available.
     * 
     * @see LogEventFactory#fromStrategies(Strategy...)
     */
    enum Strategy {
      /**
       * The SLF4J strategy.
       */
      SLF4J(Optional.of(LogEventFactory.slf4jFactory()).filter(__ -> isAvailable("org.slf4j.LoggerFactory"))),
      
      /**
       * The LOG4J strategy.
       */
      LOG4J(Optional.of(LogEventFactory.log4jFactory()).filter(__ -> isAvailable("org.apache.logging.log4j.LogManager"))),
      
      /**
       * The System.Logger strategy.
       */
      SYSTEM_LOGGER(Optional.of(LogEventFactory.systemLoggerFactory()).filter(__ -> isAvailable("java.lang.System.Logger"))),
      
      /**
       * The java.util.logging strategy.
       */
      JUL(Optional.of(LogEventFactory.julFactory()).filter(__ -> isAvailable("java.util.logging.Logger")))
      ;
      
      final Optional<LogEventFactory> factory;
      
      private Strategy(Optional<LogEventFactory> factory) {
        this.factory = factory;
      }

      private static boolean isAvailable(String className) {
        try {
          Class.forName(className);
          return true;
        } catch(@SuppressWarnings("unused") ClassNotFoundException __) {
          return false;
        }
      }
      
      static final LogEventFactory DEFAULT_FACTORY = fromStrategies(SLF4J, LOG4J, SYSTEM_LOGGER, JUL);
    }
    
    /**
     * Return the first available LogEventFactory among {@link Strategy#SLF4J}, {@link Strategy#LOG4J},
     * {@link Strategy#SYSTEM_LOGGER} and {@link Strategy#JUL}.
     * This call is equivalent to {@link LogEventFactory#fromStrategies(Strategy...) fromStrategies(SLF4J, LOG4J, SYSTEM_LOGGER, JUL)}.
     * @return the first available LogEventFactory.
     */
    static LogEventFactory defaultFactory() {
      return Strategy.DEFAULT_FACTORY;  
    }
    
    /**
     * Returns a LogEventFactory by checking checking each {@link Strategy} to find an available logger factory
     * or default to the {@link #systemLoggerFactory()}.
     * @param strategies the strategy to pick in order.
     * @return the first LogEventFactory available.  
     * @throws IllegalStateException if no LogEventFactory is available. 
     */
    static LogEventFactory fromStrategies(Strategy... strategies) {
      return Arrays.stream(strategies).flatMap(s -> s.factory.stream()).findFirst().orElseThrow(() -> new IllegalStateException("no available LogEventFactory"));
    }
    
    /**
     * Returns a LogEventFactory that uses SLF4J to log events.
     * @return a LogEventFactory that uses SLF4J to log events.
     */
    static LogEventFactory slf4jFactory() {
      return configClass -> LoggerImpl.SLF4JFactoryImpl.SLF4J_LOGGER.bindTo(org.slf4j.LoggerFactory.getLogger(configClass));
    }
    
    /**
     * Returns a LogEventFactory that uses LOG4J to log events.
     * @return a LogEventFactory that uses LOG4J to log events.
     */
    static LogEventFactory log4jFactory() {
      return configClass -> LoggerImpl.Log4JFactoryImpl.LOG4J_LOGGER.bindTo(org.apache.logging.log4j.LogManager.getLogger(configClass));
    }
    
    /**
     * Returns a LogEventFactory that uses the {@link java.lang.System.Logger system logger}.
     * @return a new LogEventFactory that delegate the logging to the {@link java.lang.System.Logger system logger}.
     */
    static LogEventFactory systemLoggerFactory() {
      return configClass -> LoggerImpl.SystemLoggerFactoryImpl.SYSTEM_LOGGER.bindTo(System.getLogger(configClass.getName()));
    }
    
    /**
     * Returns a LogEventFactory that uses java.util.logging to log events.
     * @return a LogEventFactory that uses java.util.logging to log events.
     */
    static LogEventFactory julFactory() {
      return configClass -> LoggerImpl.JULFactoryImpl.JUL_LOGGER.bindTo(java.util.logging.Logger.getLogger(configClass.getName()));
    }
  }
  
  
  /**
   *  The configuration properties that can be mutated
   *  by the method {@link LoggerConfig#update(Consumer)} of a {@link LoggerConfig}.
   */
  interface ConfigOption {
    /**
     * Update the configuration to enable/disable the loggers.
     * @param enable true to enable the logging.
     * @return this configuration option
     */
    ConfigOption enable(boolean enable);
    /**
     * Update the configuration level.
     * @param level the accepted logging level.
     * @return this configuration option
     * @throws NullPointerException if the level is null
     */
    ConfigOption level(Level level);
    /**
     * Update the configuration property logEventFactory.
     * @param factory the print factory to use
     * @return this configuration option
     * @throws NullPointerException if the factory is null
     */
    ConfigOption logEventFactory(LogEventFactory factory);
  }
  
  /**
   * Returns if the logging is enable, disable or not set.
   * @return if the logging is enable, disable or not set.
   */
  Optional<Boolean> enable();
  /**
   * Returns the logging level if set.
   * @return the logging level if set.
   */
  Optional<Level> level();
  /**
   * Returns the log event factory if set.
   * @return the log event factory if set.
   */
  Optional<LogEventFactory> logEventFactory();
  
  /**
   * Update the configuration by updating the value and then commit the changes. 
   * @param configUpdater a lambda that can update the configuration.
   * @return this logger configuration.
   */
  LoggerConfig update(Consumer<? super ConfigOption> configUpdater);
  
  /**
   * Returns the configuration associated with the configuration class.
   * @param configClass the configuration class.
   * @return the  configuration associated with the configuration class.
   * @throws NullPointerException if the configuration class is null.
   */
  static LoggerConfig fromClass(Class<?> configClass) {
    return fromClass(configClass.getName());  // implicit null check
  }
  /**
   * Returns the configuration associated with the configuration class.
   * @param className the configuration class name.
   * @return the  configuration associated with the configuration class.
   * @throws NullPointerException if the configuration class name is null.
   */
  static LoggerConfig fromClass(String className) {
    Objects.requireNonNull(className);
    return LoggerImpl.configFrom(CLASS, className);
  }
  /**
   * Returns the configuration associated with the configuration package.
   * @param packaze the configuration package.
   * @return the  configuration associated with the configuration package.
   * @throws NullPointerException if the configuration package is null.
   */
  static LoggerConfig fromPackage(Package packaze) {
    return fromPackage(packaze.getName());  // implicit null check
  }
  /**
   * Returns the configuration associated with the configuration package.
   * @param packageName the configuration package name.
   * @return the  configuration associated with the configuration package.
   * @throws NullPointerException if the configuration package name is null.
   */
  static LoggerConfig fromPackage(String packageName) {
    Objects.requireNonNull(packageName);
    return LoggerImpl.configFrom(PACKAGE, packageName);
  }
  /**
  * Returns the configuration associated with the configuration module.
  * @param module the configuration module.
  * @return the  configuration associated with the configuration module.
  * @throws NullPointerException if the configuration module is null.
  */
  /*static LoggerConfig fromModule(Module module) {  // Removed because of Java 8 support
    return fromModule(module.getName());  // implicit null check
  }*/
  /**
   * Returns the configuration associated with the configuration module.
   * @param moduleName the configuration module name.
   * @return the  configuration associated with the configuration module.
   * @throws NullPointerException if the configuration module name is null.
   */
  static LoggerConfig fromModule(String moduleName) {
    Objects.requireNonNull(moduleName);
    return LoggerImpl.configFrom(MODULE, moduleName);
  }
}

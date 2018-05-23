package com.github.forax.beautifullogger;

import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.CLASS;
import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.MODULE;
import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.PACKAGE;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
 *  <tr><td>levelOverride</td><td>false</td></tr>
 *  <tr><td>logEventFactory</td><td>{@link LogFacadeFactory#defaultFactory()}</td></tr>
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
 *    config.update(opt -&gt; opt.level(Level.TRACE, false)); 
 * </pre>
 * 
 */
public interface LoggerConfig {
  /**
   * Facade that abstract any loggers.
   */
  @FunctionalInterface
  interface LogFacade {
    /**
     * Returns a method handle that can emit a logging event for a configuration class, its method type must be
     * {@link java.lang.invoke.MethodType#methodType(Class, Class[]) MethodType#methodType(void.class, String.class, Level.class, Throwable.class)}.
     * This method is not called for each event but more or less each time
     * the runtime detects that the configuration has changed.
     * 
     * @return a method handle that can emit a logging event.
     */
    MethodHandle getLogMethodHandle();
    
    /**
     * Override the level of the underlying logger.
     * This method is not called for each event but more or less each time
     * the runtime detects that the configuration has changed.
     * 
     * @param level the new level of the underlying logger.
     * @throws UnsupportedOperationException if overriding the level is not
     *   supported by the underlying logger
     */
    default void overrideLevel(Level level) {
      throw new UnsupportedOperationException("SLF4J do not offer to change the log level dynamically");
    }
  }
  
  /**
   * The interface that provides a method handle that can emit
   * a logging event for a configuration class.
   * 
   * @see LoggerConfig#logFacadeFactory()
   * @see ConfigOption#logFacadeFactory(LogFacadeFactory)
   */
  @FunctionalInterface
  interface LogFacadeFactory {
    /**
     * Returns a log facade configured for the configuration class.
     * 
     * @param configClass the configuration class.
     * @return a newly created log facade.
     */
    LogFacade logFacade(Class<?> configClass);
    
    /**
     * A strategy represent a Logger factory that may or may not be available.
     * 
     * @see LogFacadeFactory#fromStrategies(Strategy...)
     */
    enum Strategy {
      /**
       * The SLF4J strategy.
       */
      SLF4J(Optional.of(LogFacadeFactory.slf4jFactory()).filter(__ -> isAvailable("org.slf4j.LoggerFactory"))),
      
      /**
       * The LOG4J strategy.
       */
      LOG4J(Optional.of(LogFacadeFactory.log4jFactory()).filter(__ -> isAvailable("org.apache.logging.log4j.LogManager"))),
      
      /**
       * The Logback strategy.
       */
      LOGBACK(Optional.of(LogFacadeFactory.logbackFactory()).filter(__ -> isAvailable("ch.qos.logback.classic.LoggerContext"))),
      
      /**
       * The System.Logger strategy.
       */
      SYSTEM_LOGGER(Optional.of(LogFacadeFactory.systemLoggerFactory()).filter(__ -> isAvailable("java.lang.System.Logger"))),
      
      /**
       * The java.util.logging strategy.
       */
      JUL(Optional.of(LogFacadeFactory.julFactory()).filter(__ -> isAvailable("java.util.logging.Logger")))
      ;
      
      final Optional<LogFacadeFactory> factory;
      
      private Strategy(Optional<LogFacadeFactory> factory) {
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
      
      static final LogFacadeFactory DEFAULT_FACTORY = fromStrategies(SLF4J, LOG4J, LOGBACK, SYSTEM_LOGGER, JUL);
    }
    
    /**
     * Return the first available LogEventFactory among {@link Strategy#SLF4J}, {@link Strategy#LOG4J}, {@link Strategy#LOGBACK},
     * {@link Strategy#SYSTEM_LOGGER} and {@link Strategy#JUL}.
     * This call is equivalent to {@link LogFacadeFactory#fromStrategies(Strategy...) fromStrategies(SLF4J, LOG4J, LOGBACK, SYSTEM_LOGGER, JUL)}.
     * @return the first available LogEventFactory.
     */
    static LogFacadeFactory defaultFactory() {
      return Strategy.DEFAULT_FACTORY;  
    }
    
    /**
     * Returns a LogEventFactory by checking checking each {@link Strategy} to find an available logger factory
     * or default to the {@link #systemLoggerFactory()}.
     * @param strategies the strategy to pick in order.
     * @return the first LogEventFactory available.  
     * @throws IllegalStateException if no LogEventFactory is available. 
     */
    static LogFacadeFactory fromStrategies(Strategy... strategies) {
      return Arrays.stream(strategies).flatMap(s -> asStream(s.factory)).findFirst().orElseThrow(() -> new IllegalStateException("no available LogEventFactory"));
    }
    
    private static <T> Stream<T> asStream(Optional<T> optional) {
      return optional.isPresent()? Stream.of(optional.get()): Stream.empty();
    }
    
    /**
     * Returns a LogEventFactory that uses SLF4J to log events.
     * @return a LogEventFactory that uses SLF4J to log events.
     */
    static LogFacadeFactory slf4jFactory() {
      return configClass -> {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(configClass);
        return () -> LoggerImpl.SLF4JFactoryImpl.SLF4J_LOGGER.bindTo(logger);
      };
    }
    
    /**
     * Returns a LogEventFactory that uses Log4J to log events.
     * @return a LogEventFactory that uses Log4J to log events.
     */
    static LogFacadeFactory log4jFactory() {
      return configClass -> new LoggerImpl.Log4JFactoryImpl(org.apache.logging.log4j.LogManager.getLogger(configClass));
    }
    
    /**
     * Returns a LogEventFactory that uses Logback to log events.
     * @return a LogEventFactory that uses Logback to log events.
     */
    static LogFacadeFactory logbackFactory() {
      return configClass -> {
        ch.qos.logback.classic.LoggerContext context = new ch.qos.logback.classic.LoggerContext();
        ch.qos.logback.classic.util.ContextInitializer contextInitializer = new ch.qos.logback.classic.util.ContextInitializer(context);
        try {
          contextInitializer.configureByResource(contextInitializer.findURLOfDefaultConfigurationFile(true));
        } catch(RuntimeException e) {
          throw e;
        } catch (/*Joran*/Exception e) {  // exception are loaded eagerly by the VM
          throw new IllegalStateException(e);
        }
        ch.qos.logback.classic.Logger logger = context.getLogger(configClass);
        return new LoggerImpl.LogbackFactoryImpl(logger);
      };
    }
    
    /**
     * Returns a LogEventFactory that uses the {@link java.lang.System.Logger system logger}.
     * @return a new LogEventFactory that delegate the logging to the {@link java.lang.System.Logger system logger}.
     */
    static LogFacadeFactory systemLoggerFactory() {
      return configClass -> {
        java.lang.System.Logger logger = System.getLogger(configClass.getName());
        return () -> LoggerImpl.SystemLoggerFactoryImpl.SYSTEM_LOGGER.bindTo(logger);
      };
    }
    
    /**
     * Returns a LogEventFactory that uses java.util.logging to log events.
     * @return a LogEventFactory that uses java.util.logging to log events.
     */
    static LogFacadeFactory julFactory() {
      return configClass -> new LoggerImpl.JULFactoryImpl(java.util.logging.Logger.getLogger(configClass.getName()));
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
     * 
     * @see LoggerConfig#enable()
     */
    ConfigOption enable(boolean enable);
    /**
     * Update the configuration level.
     * 
     * Note that the creation of the underlying logger is lazy so
     * if the level of the underlying logger need to be overridden
     * this operation will be delayed until the underlying logger is created.
     * 
     * @param level the accepted logging level.
     * @param override the logging level of the underlying logger.
     * @return this configuration option
     * @throws NullPointerException if the level is null
     * 
     * @see LoggerConfig#level()
     * @see LoggerConfig#levelOverride()
     */
    ConfigOption level(Level level, boolean override);
    /**
     * Update the configuration logFacadeFactory.
     * @param factory the new factory to use
     * @return this configuration option
     * @throws NullPointerException if the factory is null
     * 
     * @see LoggerConfig#logFacadeFactory()
     */
    ConfigOption logFacadeFactory(LogFacadeFactory factory);
  }
  
  /**
   * Returns if the logging is enable, disable or not set.
   * @return true if the logging is enable, disable or not set.
   * 
   * @see ConfigOption#enable(boolean)
   */
  Optional<Boolean> enable();
  /**
   * Returns the logging level if set.
   * @return the logging level if set.
   * 
   * @see ConfigOption#level(Level, boolean)
   */
  Optional<Level> level();
  /**
   * Returns if the logging level override the log level of the underlying logger.
   * @return true if the logging level override the log level of the underlying logger.
   * 
   * @see ConfigOption#level(Level, boolean)
   */
  Optional<Boolean> levelOverride();
  /**
   * Returns the log event factory if set.
   * @return the log event factory if set.
   * 
   * @see ConfigOption#logFacadeFactory(LogFacadeFactory)
   */
  Optional<LogFacadeFactory> logFacadeFactory();
  
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

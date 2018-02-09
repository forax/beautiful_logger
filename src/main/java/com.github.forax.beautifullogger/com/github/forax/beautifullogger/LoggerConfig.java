package com.github.forax.beautifullogger;

import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.CLASS;
import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.MODULE;
import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigKind.PACKAGE;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.github.forax.beautifullogger.Logger.Level;

/**
 * The configuration of a {@link Logger}.
 * 
 * The configuration can be set on a {@link LoggerConfig#fromModule(Module) module},
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
 *  <tr><td>printFactory</td><td>{@link PrintFactory#systemLogger() System Logger}</td></tr>
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
   * The interface to intercept the logging events after that logging level
   * have been been verified and the message have been computed.
   * 
   * @see PrintFactory#printer(Printer)
   */
  @FunctionalInterface
  interface Printer {
    /**
     * Emit the logging event.
     * 
     * @param message the event message
     * @param level the event level
     * @param context the event exception or null.
     */
    void print(String message, Level level, Throwable context);
  }
  
  /**
   * The interface that provides a method handle that can emit
   * a logging event for a configuration class.
   * 
   * @see LoggerConfig#printFactory()
   * @see ConfigOption#printFactory(PrintFactory)
   */
  @FunctionalInterface
  interface PrintFactory {
    /**
     * Returns a method handle that can emit a logging event for a configuration class.
     * This method is not called for each event but more or less each time
     * the runtime detects that the configuration has changed.
     * 
     * @param configClass the configuration class.
     * @return a method handle that can emit a logging event.
     */
    MethodHandle getPrintMethodHandle(Class<?> configClass);
    
    /**
     * Create a PrintFactory from a printer
     * @param printer a printer.
     * @return a new PrintFactory that delegate the logging to the printer.
     * @throws NullPointerException if the printer is null.
     */
    static PrintFactory printer(Printer printer) {
      Objects.requireNonNull(printer);
      MethodHandle mh;
      try {
        mh = MethodHandles.lookup().findVirtual(Printer.class, "print",
            MethodType.methodType(void.class, String.class, Level.class, Throwable.class)); 
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
      MethodHandle target = mh.bindTo(printer);
      return __ -> target;
    }
    
    /**
     * Create a PrintFactory from the {@link java.lang.System.Logger system logger}.
     * @return a new PrintFactory that delegate the logging to the {@link java.lang.System.Logger system logger}.
     */
    static PrintFactory systemLogger() {
      MethodHandle mh, filter;
      try {
        mh = MethodHandles.publicLookup().findVirtual(System.Logger.class, "log",
            MethodType.methodType(void.class, System.Logger.Level.class, String.class, Throwable.class));
        filter = MethodHandles.lookup().findStatic(PrintFactory.class, "level",
            MethodType.methodType(System.Logger.Level.class, Level.class)); 
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
      mh = MethodHandles.filterArguments(mh, 1, filter);
      MethodHandle target = MethodHandles.permuteArguments(mh,
          MethodType.methodType(void.class, System.Logger.class, String.class, Level.class, Throwable.class),
          new int[] { 0, 2, 1, 3});
      return configClass -> target.bindTo(System.getLogger(configClass.getName()));
    }
    
    @SuppressWarnings("unused")
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
  
  
  /**
   *  The option that can be mutated by the method {@link LoggerConfig#update(Consumer)}
   *  of a {@link LoggerConfig}.
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
     * Update the configuration property enable.
     * @param factory the print factory to use
     * @return this configuration option
     * @throws NullPointerException if the factory is null
     */
    ConfigOption printFactory(PrintFactory factory);
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
   * Returns the logging print factory if set.
   * @return the logging print factory if set.
   */
  Optional<PrintFactory> printFactory();
  
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
  static LoggerConfig fromModule(Module module) {
    return fromModule(module.getName());  // implicit null check
  }
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

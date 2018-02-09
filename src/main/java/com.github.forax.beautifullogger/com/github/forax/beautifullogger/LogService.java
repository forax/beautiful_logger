package com.github.forax.beautifullogger;

import java.lang.invoke.MethodHandles.Lookup;

import com.github.forax.beautifullogger.Logger.Level;

/**
 * An interface that you can extend if you want to create your own log service.
 */
public interface LogService {
  /**
   * Log a message provided by the message provider at a level with a context,
   * this method is provided when implementing a log service,  
   * this method is not recommended for direct use.
   * 
   * @param level the logging level that will be checked against the {@link LoggerConfig#level() logger configuration level}.
   * @param context the exception to log or null. 
   * @param messageProvider a message provider, a String or a lambda.
   * @param arg0 the first argument or {@link LogServiceSPI#NONE}.
   * @param arg1 the second argument or {@link LogServiceSPI#NONE}.
   * @param arg2 the third argument or {@link LogServiceSPI#NONE}.
   * @param arg3 the fourth argument or {@link LogServiceSPI#NONE}.
   */
  void log(Level level, Throwable context, Object messageProvider, Object arg0, Object arg1, Object arg2, Object arg3);

  /**
   * A null object that represent no argument.
   * @see LogService#log(Level, Throwable, Object, Object, Object, Object, Object)
   */
  Object NONE = LoggerImpl.NONE;

  /**
   * Create a log service from a lookup object and the interface of the service.
   * 
   * Calling this method is equivalent a call to
   * {@link #getService(Lookup, Class, Class) getService(lookup, serviceInterface, serviceInterface)}.
   * 
   * @param lookup a lookup object that have access to the service interface.
   * @param serviceInterface the service interface to implement.
   * @return an object of a class that implement that service.
   */
  static <T> T getService(Lookup lookup, Class<T> serviceInterface) {
    return getService(lookup, serviceInterface, serviceInterface);
  }

  /**
   * Create a log service from a lookup object and the interface of the service.
   * 
   * Each abstract method of the interface has to be a method with the same name and the same signature
   * as either a method in the interface {@link Logger} or a method in the interface {@link LogService},
   * otherwise an AbstractMethodError will be thrown when the method will be called.
   * 
   * The service interface can have default methods, if a default method as the same name and signature
   * as one abstract method of the interface {@link Logger} or the interface {@link LogService},
   * the default method will be overridden.
   * 
   * @param lookup a lookup object that have access to the service interface.
   * @param serviceInterface the service interface to implement.
   * @param configClass class that will use to find the configuration of the log service. 
   * @return an object of a class that implement that service.
   */
  static <T> T getService(Lookup lookup, Class<T> serviceInterface, Class<?> configClass) {
    if (!serviceInterface.isInterface()) {
      throw new IllegalArgumentException("the service type should be an interface");
    }
    return serviceInterface.cast(
        LoggerImpl.LogServiceImpl.getService(lookup, serviceInterface, configClass));
  }
}

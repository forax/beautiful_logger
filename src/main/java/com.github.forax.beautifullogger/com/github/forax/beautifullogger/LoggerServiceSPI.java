package com.github.forax.beautifullogger;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;

public class LoggerServiceSPI {
  private LoggerServiceSPI() {
    throw new AssertionError();
  }
  
  //null object
  public static final Object NONE = LoggerImpl.NONE;

  public static MethodHandle getLoggingMethodHandle(Class<?> configurationClass, int maxParameter) {
    return LoggerImpl.getLoggingMethodHandle(configurationClass, maxParameter);
  }
  
  public static UndeclaredThrowableException rethrow(Throwable e) {
    return LoggerImpl.rethrow(e);
  }
}

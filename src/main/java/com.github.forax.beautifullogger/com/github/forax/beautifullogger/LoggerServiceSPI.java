package com.github.forax.beautifullogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

public class LoggerServiceSPI {
  private LoggerServiceSPI() {
    throw new AssertionError();
  }
  
  private static final ClassValue<Class<?>> SERVICE_IMPLS = new ClassValue<>() {
    @Override
    protected Class<?> computeValue(Class<?> type) {
      return LoggerImpl.loggerClass(type, new Object[] { null, type.getName().replace('.', '/') });
    }
  };
  
  //null object
  public static final Object NONE = LoggerImpl.NONE;

  public static <T> T getService(Class<T> serviceType) {
    Lookup lookup = MethodHandles.publicLookup();
    try {
      lookup.accessClass(serviceType);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
    return getService(lookup, serviceType, serviceType);
  }
  
  public static <T> T getService(Lookup lookup, Class<T> serviceType) {
    return getService(lookup, serviceType, serviceType);
  }
  
  public static <T> T getService(Lookup lookup, Class<T> serviceType, Class<?> configClass) {
    Class<?> implClass = SERVICE_IMPLS.get(serviceType);
    MethodHandle factory = LoggerImpl.createFactory(lookup, implClass);
    MethodHandle mh = LoggerImpl.getLoggingMethodHandle(configClass, 4);
    Object service;
    try {
      service = factory.invoke(mh);
    } catch (Throwable e) {
      throw LoggerImpl.rethrow(e);
    }
    return serviceType.cast(service);
  }
}

package com.github.forax.beautifullogger.tool;

import java.util.function.Supplier;

/**
 * Code use as a template to access to the caller class when available.
 */
public final class ReflectionSupplier implements Supplier<Class<?>> {
  /**
   * Creates a ReflectionSupplier.
   */
  public ReflectionSupplier() {
  }

  @Override
  public Class<?> get() {
    //return sun.reflect.Reflection.getCallerClass(5);
    throw new UnsupportedOperationException();
  }
}

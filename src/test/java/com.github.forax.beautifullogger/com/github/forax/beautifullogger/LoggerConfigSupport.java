package com.github.forax.beautifullogger;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Objects;

import com.github.forax.beautifullogger.Logger.Level;
import com.github.forax.beautifullogger.LoggerConfig.PrintFactory;

class LoggerConfigSupport {
  /**
   * The interface to intercept the logging events after that logging level
   * have been been verified and the message have been computed.
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
  
  static final MethodHandle PRINTER_PRINT;
  static {
    Lookup lookup = MethodHandles.lookup();
    try {
      PRINTER_PRINT = lookup.findVirtual(Printer.class, "print", methodType(void.class, String.class, Level.class, Throwable.class)); 
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }
  
  /**
   * Create a PrintFactory from a printer
   * @param printer a printer.
   * @return a new PrintFactory that delegate the logging to the printer.
   * @throws NullPointerException if the printer is null.
   */
  static PrintFactory printer(Printer printer) {
    Objects.requireNonNull(printer);
    MethodHandle target = PRINTER_PRINT.bindTo(printer);
    return __ -> target;
  }
}

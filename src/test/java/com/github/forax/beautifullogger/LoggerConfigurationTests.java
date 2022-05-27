package com.github.forax.beautifullogger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static com.github.forax.beautifullogger.LoggerConfigSupport.printer;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.github.forax.beautifullogger.Logger;
import com.github.forax.beautifullogger.LoggerConfig;
import com.github.forax.beautifullogger.Logger.Level;
import com.github.forax.beautifullogger.LoggerConfigSupport.Printer;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

@SuppressWarnings("static-method")
public class LoggerConfigurationTests {
  @Test
  public void loggerDisableAtCreationTime() {
    Logger logger = Logger.getLogger(
        new Object() { }.getClass(),
        upd -> upd.enable(false).logFacadeFactory(printer((message, loggerLevel, context) -> {
          fail("logger shouble be disable");
        })));
    logger.debug("exception", null);
    logger.error("exception", null);
    logger.info("exception", null);
    logger.trace("exception", null);
    logger.warning("exception", null);
  }
  
  @Test
  public void loggerDisableAfterCreationTime() {
    var confClass = new Object() { }.getClass();
    Logger logger = Logger.getLogger(
        confClass,
        upd -> upd.logFacadeFactory(printer((message, loggerLevel, context) -> {
          fail("logger shouble be disable");
        })));
    LoggerConfig.fromClass(confClass).update(upd -> upd.enable(false));
    logger.debug("exception", null);
    logger.error("exception", null);
    logger.info("exception", null);
    logger.trace("exception", null);
    logger.warning("exception", null);
  }
  
  @Test
  public void loggerEnableThenDisable() {
    Class<?> confClass = new Object() { }.getClass();
    class MyPrinter implements Printer {
      boolean disable;
      
      @Override
      public void print(String message, Level level, Throwable context) {
        if (disable) {
          fail("the logger is disable");
        } else {
          assertAll(
              () -> assertEquals(Level.ERROR, level),
              () -> assertEquals("message", message),
              () -> assertNull(context));
        }
      }
    }
    MyPrinter printer = new MyPrinter();
    Logger logger = Logger.getLogger(confClass, upd -> upd.logFacadeFactory(printer(printer)));
    
    for(int i = 0; i < 100_000; i++) {
      logger.error(() -> "message");
      if (i == 20_000) {
        LoggerConfig.fromClass(confClass).update(upd -> upd.enable(false));
        printer.disable = true;
      }
    }
  }
}

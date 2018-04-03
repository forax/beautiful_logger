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

@SuppressWarnings("static-method")
class LoggerConfigurationTests {
  @Test
  void loggerDisableAtCreationTime() {
    Logger logger = Logger.getLogger(
        new Object() {/*empty*/}.getClass(),
        opt -> opt.enable(false).printFactory(printer((message, loggerLevel, context) -> {
          fail("logger shouble be disable");
        })));
    logger.debug("exception", null);
    logger.error("exception", null);
    logger.info("exception", null);
    logger.trace("exception", null);
    logger.warning("exception", null);
  }
  
  @Test
  void loggerDisableAfterCreationTime() {
    Class<?> clazz = new Object() {/*empty*/}.getClass(); 
    Logger logger = Logger.getLogger(
        clazz,
        opt -> opt.printFactory(printer((message, loggerLevel, context) -> {
          fail("logger shouble be disable");
        })));
    LoggerConfig.fromClass(clazz).update(opt -> opt.enable(false));
    logger.debug("exception", null);
    logger.error("exception", null);
    logger.info("exception", null);
    logger.trace("exception", null);
    logger.warning("exception", null);
  }
  
  @Test
  void loggerEnableThenDisable() {
    Class<?> clazz = new Object() {/*empty*/}.getClass();
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
    Logger logger = Logger.getLogger(clazz, opt -> opt.printFactory(printer(printer)));
    
    for(int i = 0; i < 100_000; i++) {
      logger.error(() -> "message");
      if (i == 20_000) {
        LoggerConfig.fromClass(clazz).update(opt -> opt.enable(false));
        printer.disable = true;
      }
    }
  }
}

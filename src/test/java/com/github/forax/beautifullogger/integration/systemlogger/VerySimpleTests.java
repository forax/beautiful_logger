package com.github.forax.beautifullogger.integration.systemlogger;

import static com.github.forax.beautifullogger.Logger.Level.DEBUG;
import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.github.forax.beautifullogger.Logger;
import com.github.forax.beautifullogger.LoggerConfig;
import com.github.forax.beautifullogger.LoggerConfig.LogFacadeFactory;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.JRE;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

@SuppressWarnings("static-method")
@DisabledOnJre(JRE.JAVA_8)
public class VerySimpleTests {
  private static final Logger LOGGER = Logger.getLogger();

  @Test
  public void justAVerySimpleTest() {
    LoggerConfig config = LoggerConfig.fromClass(VerySimpleTests.class);
    config.update(upd -> upd.logFacadeFactory(LogFacadeFactory.systemLoggerFactory()));
    
    for(int i = 0; i < 10; i++) {
      LOGGER.error((int value) -> "message " + value, i);
      
      if (i == 1) {
        config.update(upd -> upd.enable(false));
      }
    }
  }
  
  @Test
  public void overrideLevel() {
    class Conf { /* empty */ }
    LoggerConfig config = LoggerConfig.fromClass(Conf.class);
    config.update(upd -> upd.logFacadeFactory(LogFacadeFactory.systemLoggerFactory()).level(DEBUG, true));
    Logger logger = Logger.getLogger(Conf.class);
    assertThrows(UnsupportedOperationException.class, () -> {
      logger.debug("this message should not be printed", null);  
    });
  }
}

package com.github.forax.beautifullogger.integration.jul;

import static com.github.forax.beautifullogger.Logger.Level.DEBUG;

import org.junit.jupiter.api.Test;

import com.github.forax.beautifullogger.Logger;
import com.github.forax.beautifullogger.LoggerConfig;
import com.github.forax.beautifullogger.LoggerConfig.LogFacadeFactory;

@SuppressWarnings("static-method")
class VerySimpleTests {
  private static final Logger LOGGER = Logger.getLogger();
  
  @Test
  void justAVerySimpleTest() {
    LoggerConfig config = LoggerConfig.fromClass(VerySimpleTests.class);
    config.update(upd -> upd.logFacadeFactory(LogFacadeFactory.julFactory()));
    
    for(int i = 0; i < 10; i++) {
      LOGGER.error((int value) -> "message " + value, i);
      
      if (i == 1) {
        config.update(upd -> upd.enable(false));
      }
    }
  }
  
  private static void updateDefaultsLevel(java.util.logging.Level level) {
    for (java.util.logging.Handler handler: java.util.logging.Logger.getLogger("").getHandlers()) {
      handler.setLevel(level);
    }
  }
  
  @Test
  void overrideLevel() {
    class Conf { /* empty */ }
    
    updateDefaultsLevel(java.util.logging.Level.FINE);
    try {
      LoggerConfig config = LoggerConfig.fromClass(Conf.class);
      config.update(upd -> upd.logFacadeFactory(LogFacadeFactory.julFactory()).level(DEBUG, true));
      Logger logger = Logger.getLogger(Conf.class);
      logger.debug("JUL override ok !", null);
    } finally {
      updateDefaultsLevel(java.util.logging.Level.INFO);
    }
  }
}

package com.github.forax.beautifullogger.integration.log4j;

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
    config.update(upd -> upd.logFacadeFactory(LogFacadeFactory.log4jFactory()));
    
    for(int i = 0; i < 10; i++) {
      LOGGER.error((int value) -> "message " + value, i);
      
      if (i == 1) {
        config.update(upd -> upd.enable(false));
      }
    }
  }
  
  @Test
  void overrideLevel() {
    class Conf { /* empty */ }
    LoggerConfig config = LoggerConfig.fromClass(Conf.class);
    config.update(upd -> upd.logFacadeFactory(LogFacadeFactory.log4jFactory()).level(DEBUG, true));
    Logger logger = Logger.getLogger(Conf.class);
    logger.debug("Log4J override ok !", null);
  }
}

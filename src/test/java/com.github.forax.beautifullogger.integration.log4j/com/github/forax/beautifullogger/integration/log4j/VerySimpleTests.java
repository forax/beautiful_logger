package com.github.forax.beautifullogger.integration.log4j;

import org.junit.jupiter.api.Test;

import com.github.forax.beautifullogger.Logger;
import com.github.forax.beautifullogger.LoggerConfig;
import com.github.forax.beautifullogger.LoggerConfig.LogEventFactory;

@SuppressWarnings("static-method")
class VerySimpleTests {
  private static final Logger LOGGER = Logger.getLogger();
  
  @Test
  void justAVerySimpleTest() {
    LoggerConfig config = LoggerConfig.fromClass(VerySimpleTests.class);
    config.update(opt -> opt.logEventFactory(LogEventFactory.log4jFactory()));
    
    for(int i = 0; i < 10; i++) {
      try {
      LOGGER.error((int value) -> "message " + value, i);
      } catch(Error | RuntimeException e) {
        e.printStackTrace();
        throw e;
      }
      
      if (i == 1) {
        config.update(opt -> opt.enable(false));
      }
    }
  }
}

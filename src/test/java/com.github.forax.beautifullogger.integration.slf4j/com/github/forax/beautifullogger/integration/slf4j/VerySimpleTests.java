package com.github.forax.beautifullogger.integration.slf4j;

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
    config.update(upd -> upd.logEventFactory(LogEventFactory.slf4jFactory()));
    
    for(int i = 0; i < 10; i++) {
      LOGGER.error((int value) -> "message " + value, i);
      
      if (i == 1) {
        config.update(upd -> upd.enable(false));
      }
    }
  }
}

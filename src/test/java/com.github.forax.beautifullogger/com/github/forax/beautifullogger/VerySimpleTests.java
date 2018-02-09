package com.github.forax.beautifullogger;

import org.junit.jupiter.api.Test;

import com.github.forax.beautifullogger.Logger;

@SuppressWarnings("static-method")
class VerySimpleTests {
  private static final Logger LOGGER = Logger.getLogger();
  
  @Test
  void justAVerySimpleTest() {
    for(int i = 0; i < 10; i++) {
      LOGGER.error((int value) -> "message " + value, i);
      
      if (i == 1) {
        LoggerConfig.fromClass(VerySimpleTests.class)
          .update(opt -> opt.enable(false));
      }
    }
  }
}

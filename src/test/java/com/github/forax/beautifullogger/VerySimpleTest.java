package com.github.forax.beautifullogger;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class VerySimpleTest {
  private static final Logger LOGGER = Logger.getLogger(VerySimpleTest.class);
  
  @Test
  void justAVerySimpleTest() {
    LOGGER.error(() -> "message");
  }
}

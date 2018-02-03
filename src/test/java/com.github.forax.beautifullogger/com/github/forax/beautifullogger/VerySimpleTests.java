package com.github.forax.beautifullogger;

import org.junit.jupiter.api.Test;

import com.github.forax.beautifullogger.Logger;

@SuppressWarnings("static-method")
class VerySimpleTests {
  private static final Logger LOGGER = Logger.getLogger(VerySimpleTests.class);
  
  @Test
  void justAVerySimpleTest() {
    LOGGER.error(() -> "message");
  }
}

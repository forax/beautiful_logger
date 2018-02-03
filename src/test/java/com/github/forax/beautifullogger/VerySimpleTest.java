package com.github.forax.beautifullogger;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class VerySimpleTest {
  private static final Logger LOGGER = Logger.getLogger(VerySimpleTest.class);
  
  @Test
  public void justAVerySimpleTest() {
    LOGGER.error(() -> "message");
  }
}

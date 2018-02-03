package com.github.forax.beautifullogger;

import org.junit.jupiter.api.Test;

//FIXME use JMH instead
@SuppressWarnings("static-method")
public class LoggerPerfTest {
  private static class Perf1 {
    static int sinkHole;
    
    static final Logger LOGGER = Logger.getLogger(Perf1.class, opt -> opt.printer((message, level, context) -> {
      sinkHole++;
    }));
  }
  
  @Test
  public void perfDynamicDisable() {
    for(int i = 0; i < 1_000_000; i++) {
      Perf1.LOGGER.error(() -> "message");
      if (i == 500_000) {
        LoggerConfig.fromClass(Perf1.class).update(opt -> opt.enable(false));
      }
    }
  }
}

package com.github.forax.beautifullogger;

import static com.github.forax.beautifullogger.LoggerConfig.PrintFactory.printer;

import org.junit.jupiter.api.Test;

import com.github.forax.beautifullogger.Logger;
import com.github.forax.beautifullogger.LoggerConfig;

//FIXME use JMH instead
@SuppressWarnings("static-method")
class LoggerPerfTests {
  private static class Perf1 {
    static int sinkHole;
    
    static final Logger LOGGER = Logger.getLogger(Perf1.class, opt -> opt.printFactory(printer((message, level, context) -> {
      sinkHole++;
    })));
  }
  
  @Test
  void perfDynamicDisable() {
    for(int i = 0; i < 1_000_000; i++) {
      Perf1.LOGGER.error(() -> "message");
      if (i == 500_000) {
        LoggerConfig.fromClass(Perf1.class).update(opt -> opt.enable(false));
      }
    }
  }
}

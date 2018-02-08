package com.github.forax.beautifullogger;

import static com.github.forax.beautifullogger.LoggerConfig.PrintFactory.printer;
import static com.github.forax.beautifullogger.LoggerServiceSPI.NONE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.invoke.MethodHandles;
import java.security.Principal;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.forax.beautifullogger.Logger.Level;

@SuppressWarnings("static-method")
class LoggerServiceSPITests {
  interface MethodLoggerService extends Logger {
    default void logEnter() {
      info("enter", null);
    }
    default void logExit() {
      info("exit", null);
    }
    
    static MethodLoggerService getService() {
      return LoggerServiceSPI.getService(MethodHandles.lookup(), MethodLoggerService.class);
    }
  }
  @Test
  void testUserDefinedMethodLoggerService() {
    MethodLoggerService service = MethodLoggerService.getService();
    LoggerConfig config = LoggerConfig.fromClass(MethodLoggerService.class);
    
    config.update(opt -> opt.printFactory(printer((message, level, context) -> {
      assertAll(  
        () -> assertEquals(Level.INFO, level),
        () -> assertEquals("enter", message),
        () -> assertNull(context)
        );
    })));
    service.logEnter();
    
    config.update(opt -> opt.printFactory(printer((message, level, context) -> {
      assertAll(  
        () -> assertEquals(Level.INFO, level),
        () -> assertEquals("exit", message),
        () -> assertNull(context)
        );
    })));
    service.logExit();
  }
  
  interface AuthLoggerService {
    void log(Level level, Throwable context, Object messageProvider, Object arg0, Object arg1, Object arg2, Object arg3);
    
    default void authorized(Principal principal) {
      log(Level.TRACE, null, (Function<Principal, String>)p -> p + " authorized", principal, NONE, NONE, NONE);
    }
    default void unauthorized(Principal principal, String reason) {
      log(Level.INFO, null, (BiFunction<Principal, String, String>)(p, r) -> p + " unauthorized " + r, principal, reason, NONE, NONE);
    }
    
    static AuthLoggerService getService() {
      return LoggerServiceSPI.getService(MethodHandles.lookup(), AuthLoggerService.class);
    }
  }
  enum People implements Principal {
    bob, amy;
    
    @Override
    public String getName() {
      return name();
    }
  }
  @Test
  void testUserDefinedAuthLoggerService() {
    AuthLoggerService service = AuthLoggerService.getService();
    LoggerConfig config = LoggerConfig.fromClass(AuthLoggerService.class)
        .update(opt -> opt.level(Level.TRACE));
    
    boolean[] called1 = { false };
    config.update(opt -> opt.printFactory(printer((message, level, context) -> {
      called1[0] = true;
      assertAll(  
        () -> assertEquals(Level.TRACE, level),
        () -> assertEquals("bob authorized", message),
        () -> assertNull(context)
        );
    })));
    service.authorized(People.bob);
    assertTrue(called1[0]);
    
    boolean[] called2 = { false };
    config.update(opt -> opt.printFactory(printer((message, level, context) -> {
      called2[0] = true;
      assertAll(  
        () -> assertEquals(Level.INFO, level),
        () -> assertEquals("amy unauthorized invalid password", message),
        () -> assertNull(context)
        );
    })));
    service.unauthorized(People.amy, "invalid password");
    assertTrue(called2[0]);
  }
}

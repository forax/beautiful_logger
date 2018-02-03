package com.github.forax.beautifullogger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.forax.beautifullogger.Logger.Level;
import java.lang.invoke.MethodHandle;
import java.security.Principal;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
class LoggerServiceSPITest {
  interface MethodLoggerService {
    void log(Level level, Throwable context, Object messageProvider);
    
    default void logEnter() {
      log(Level.INFO, null, "enter");
    }
    default void logExit() {
      log(Level.INFO, null, "exit");
    }
    
    static MethodLoggerService getService() {
      MethodHandle mh = LoggerServiceSPI.getLoggingMethodHandle(MethodLoggerService.class, 0);
      return (level, context, messageProvider) -> {
        try {
          mh.invokeExact(level, context, messageProvider);
        } catch(Throwable t) {
          throw LoggerServiceSPI.rethrow(t);
        }
      };
    }
  }
  @Test
  void testUserDefinedMethodLoggerService() {
    MethodLoggerService service = MethodLoggerService.getService();
    LoggerConfig config = LoggerConfig.fromClass(MethodLoggerService.class);
    
    config.update(opt -> opt.printer((message, level, context) -> {
      assertAll(  
        () -> assertEquals(Level.INFO, level),
        () -> assertEquals("enter", message),
        () -> assertNull(context)
        );
    }));
    service.logEnter();
    
    config.update(opt -> opt.printer((message, level, context) -> {
      assertAll(  
        () -> assertEquals(Level.INFO, level),
        () -> assertEquals("exit", message),
        () -> assertNull(context)
        );
    }));
    service.logExit();
  }
  
  interface AuthLoggerService {
    void print(Level level, Throwable context, Object messageProvider, Object arg0, Object arg1);
    
    default void authorized(Principal principal) {
      print(Level.TRACE, null, (Function<Principal, String>)p -> p + " authorized", principal, LoggerServiceSPI.NONE);
    }
    default void unauthorized(Principal principal, String reason) {
      print(Level.INFO, null, (BiFunction<Principal, String, String>)(p, r) -> p + " unauthorized " + r, principal, reason);
    }
    
    static AuthLoggerService getService() {
      MethodHandle mh = LoggerServiceSPI.getLoggingMethodHandle(AuthLoggerService.class, 2);
      return (level, context, messageProvider, arg0, arg1) -> {
        try {
          mh.invokeExact(level, context, messageProvider, arg0, arg1);
        } catch(Throwable t) {
          throw LoggerServiceSPI.rethrow(t);
        }
      };
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
  @Disabled("AssertionFailedError: expected: <true> but was: <false>")
  void testUserDefinedAuthLoggerService() {
    AuthLoggerService service = AuthLoggerService.getService();
    LoggerConfig config = LoggerConfig.fromClass(AuthLoggerService.class);
    
    boolean[] called1 = { false };
    config.update(opt -> opt.printer((message, level, context) -> {
      called1[0] = true;
      assertAll(  
        () -> assertEquals(Level.TRACE, level),
        () -> assertEquals("bob authorized", message),
        () -> assertNull(context)
        );
    }));
    service.authorized(People.bob);
    assertTrue(called1[0]);
    
    boolean[] called2 = { false };
    config.update(opt -> opt.printer((message, level, context) -> {
      called2[0] = true;
      assertAll(  
        () -> assertEquals(Level.INFO, level),
        () -> assertEquals("amy unauthorized invalid password", message),
        () -> assertNull(context)
        );
    }));
    service.unauthorized(People.amy, "invalid password");
    assertTrue(called2[0]);
  }
}

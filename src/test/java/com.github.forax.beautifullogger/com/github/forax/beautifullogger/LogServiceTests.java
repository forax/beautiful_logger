package com.github.forax.beautifullogger;

import static com.github.forax.beautifullogger.LoggerConfigSupport.printer;
import static com.github.forax.beautifullogger.LogService.NONE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.invoke.MethodHandles;
import java.security.Principal;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.forax.beautifullogger.Logger.Level;
import com.github.forax.beautifullogger.LogService;

@SuppressWarnings("static-method")
class LogServiceTests {
  interface MethodLoggerService extends Logger {
    default void logEnter() {
      info("enter", null);
    }
    default void logExit() {
      info("exit", null);
    }
    
    static MethodLoggerService getService() {
      return LogService.getService(MethodHandles.lookup(), MethodLoggerService.class);
    }
  }
  @Test
  void testUserDefinedMethodLoggerService() {
    MethodLoggerService service = MethodLoggerService.getService();
    LoggerConfig config = LoggerConfig.fromClass(MethodLoggerService.class);
    
    config.update(upd -> upd.logFacadeFactory(printer((message, level, context) -> {
      assertAll(  
        () -> assertEquals(Level.INFO, level),
        () -> assertEquals("enter", message),
        () -> assertNull(context)
        );
    })));
    service.logEnter();
    
    config.update(upd -> upd.logFacadeFactory(printer((message, level, context) -> {
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
      return LogService.getService(MethodHandles.lookup(), AuthLoggerService.class);
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
        .update(upd -> upd.level(Level.TRACE, false));
    
    boolean[] called1 = { false };
    config.update(upd -> upd.logFacadeFactory(printer((message, level, context) -> {
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
    config.update(upd -> upd.logFacadeFactory(printer((message, level, context) -> {
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
  
  @Test
  void testLoggerService() {
    Class<?> localClass = new Object() { /* empty */}.getClass();
    LogService service = LogService.getService(MethodHandles.lookup(), LogService.class, localClass);
    
    boolean[] called1 = { false };
    LoggerConfig.fromClass(localClass).update(upd -> upd.logFacadeFactory(printer((message, level, context) -> {
      called1[0] = true;
      assertAll(  
        () -> assertEquals(Level.ERROR, level),
        () -> assertEquals("logger service test", message),
        () -> assertNull(context)
        );
    })));
    service.log(Level.ERROR, null, "logger service test", NONE, NONE, NONE, NONE);
    assertTrue(called1[0]);
  }
  
  interface BadServiceInterface {
    void foo();
  }
  @Test
  void testBadLoggerService() {
    BadServiceInterface service = LogService.getService(MethodHandles.lookup(), BadServiceInterface.class);
    assertThrows(AbstractMethodError.class, () -> service.foo());
  }
}

module com.github.forax.beautifullogger {
  requires static jdk.unsupported;  // Java 17 does not use Unsafe anymore
  
  requires static org.apache.logging.log4j;  // Log4J support
  requires static org.slf4j;                 // SLF4J support
  requires static ch.qos.logback.classic;    // Logback support
  requires static ch.qos.logback.core;
  requires static java.logging;              // JUL support

  requires static org.objectweb.asm;  // temporary fix (FIXME)
                                      // never used at runtime

  exports com.github.forax.beautifullogger;
}
module com.github.forax.beautifullogger {
  requires jdk.unsupported;
  
  requires static org.apache.logging.log4j;  // Log4J support
  requires static org.slf4j;                 // SLF4J support
  requires static ch.qos.logback.classic;    // Logback support
  requires static ch.qos.logback.core;       
  requires static java.logging;              // JUL support
  
  exports com.github.forax.beautifullogger;
}

module com.github.forax.beautifullogger {
  requires jdk.unsupported;
  
  requires static org.apache.logging.log4j;  // LOG4J support
  requires static org.slf4j;                 // SLF4J support
  
  exports com.github.forax.beautifullogger;
}

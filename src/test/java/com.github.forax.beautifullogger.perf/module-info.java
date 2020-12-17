module com.github.forax.beautifullogger.perf {
  requires org.openjdk.jmh;
  requires org.openjdk.jmh.generator;  // annotation processor
  
  requires com.github.forax.beautifullogger;
  requires org.apache.logging.log4j;
  requires org.slf4j;
  requires ch.qos.logback.classic;
  requires java.logging;
  requires com.google.common.flogger;
  
  exports com.github.forax.beautifullogger.perf.jmh_generated;  // export JMH generated package
}

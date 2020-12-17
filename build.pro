import static com.github.forax.pro.Pro.*;
import static com.github.forax.pro.builder.Builders.*;

var rewriter = command("rewriter", () -> {  // rewrite bytecode to be compatible with 8
  runner.
    modulePath(path("target/main/exploded", "deps")).
    module("com.github.forax.beautifullogger.tool/com.github.forax.beautifullogger.tool.Rewriter").
    mainArguments("target/main/exploded/com.github.forax.beautifullogger");

  run(runner);
})

resolver.
  checkForUpdate(true).
  dependencies(
    // ASM
    "org.objectweb.asm=org.ow2.asm:asm:9.0",

    // JUnit
    "org.junit.jupiter.api=org.junit.jupiter:junit-jupiter-api:5.7.0",
    "org.junit.jupiter.params=org.junit.jupiter:junit-jupiter-params:5.7.0",
    "org.junit.platform.commons=org.junit.platform:junit-platform-commons:1.7.0",
    "org.apiguardian.api=org.apiguardian:apiguardian-api:1.0.0",
    "org.opentest4j=org.opentest4j:opentest4j:1.2.0",

    // JMH
    "org.openjdk.jmh=org.openjdk.jmh:jmh-core:1.27",
    "org.apache.commons.math3=org.apache.commons:commons-math3:3.2",
    "net.sf.jopt-simple=net.sf.jopt-simple:jopt-simple:4.6",
    "org.openjdk.jmh.generator=org.openjdk.jmh:jmh-generator-annprocess:1.27",

    // Log4j2
    "org.apache.logging.log4j=org.apache.logging.log4j:log4j-api:2.14.0",
    "org.apache.logging.log4j.core=org.apache.logging.log4j:log4j-core:2.14.0",

    // SLF4J + Logback
    "org.slf4j=org.slf4j:slf4j-api:1.8.0-beta4",
    "java.activation=javax.activation:activation:1.1.1",
    "java.mail=com.sun.mail:javax.mail:1.6.2",
    "ch.qos.logback.classic=ch.qos.logback:logback-classic:1.3.0-alpha4",
    "ch.qos.logback.core=ch.qos.logback:logback-core:1.3.0-alpha4",

    // Google Flogger
    "com.google.common.flogger=com.google.flogger:flogger:0.5.1,com.google.flogger:flogger-system-backend:0.5.1",
    "org.checkerframework=org.checkerframework:checker-compat-qual:2.5.3"
  )

modulefixer.
  additionalRequires("org.apache.logging.log4j.core=java.activation/true")

compiler.
  rawArguments(
    "--processor-module-path", "deps",     // enable JMH annotation processor
    "--default-module-for-created-files", "com.github.forax.beautifullogger.perf"
  )

tester.
  parallel(false)   // several tests are not threadsafe because they modify the loggers conf attached to classes

docer.
  quiet(true).
  link(uri("https://docs.oracle.com/en/java/javase/11/docs/api/"))

packager.
  modules(
    "com.github.forax.beautifullogger@0.9.12",
    "com.github.forax.beautifullogger.tool@0.9.12",
    "com.github.forax.beautifullogger.perf@0.9.12",
    "com.github.forax.beautifullogger.integration.log4j@0.9.12",
    "com.github.forax.beautifullogger.integration.slf4j@0.9.12",
    "com.github.forax.beautifullogger.integration.jul@0.9.12"
  )

run(resolver, modulefixer, compiler, rewriter, tester, docer, packager)

pro.arguments().forEach(plugin -> run(plugin))   // run plugins specified on the command line

/exit

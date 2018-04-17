import static com.github.forax.pro.Pro.*;
import static com.github.forax.pro.builder.Builders.*;

resolver.
    dependencies(list(
        // ASM
        "org.objectweb.asm=org.ow2.asm:asm:6.1.1",
        
        // JUnit
        "org.junit.jupiter.api=org.junit.jupiter:junit-jupiter-api:5.1.0",
        "org.junit.jupiter.params=org.junit.jupiter:junit-jupiter-params:5.1.0",
        "org.junit.platform.commons=org.junit.platform:junit-platform-commons:1.10",
        "org.apiguardian.api=org.apiguardian:apiguardian-api:1.0.0",
        "org.opentest4j=org.opentest4j:opentest4j:1.0.0",
        
        // JMH
        "org.openjdk.jmh=org.openjdk.jmh:jmh-core:1.20",
        "org.apache.commons.math3=org.apache.commons:commons-math3:3.6.1",
        "net.sf.jopt-simple=net.sf.jopt-simple:jopt-simple:5.0.4",
        "org.openjdk.jmh.generator=org.openjdk.jmh:jmh-generator-annprocess:1.20",
        
        // Log4j2
        "org.apache.logging.log4j=org.apache.logging.log4j:log4j-api:2.11.0",
        "org.apache.logging.log4j.core=org.apache.logging.log4j:log4j-core:2.11.0",
        
        // SLF4J + Logback
        "org.slf4j=org.slf4j:slf4j-api:1.8.0-beta2",
        "java.activation=javax.activation:activation:1.1.1",
        "java.mail=com.sun.mail:javax.mail:1.6.1",
        "ch.qos.logback.classic=ch.qos.logback:logback-classic:1.3.0-alpha4",
        "ch.qos.logback.core=ch.qos.logback:logback-core:1.3.0-alpha4"
    ))
    
modulefixer.
  additionalRequires(list("org.apache.logging.log4j.core=java.activation/true"))

compiler.
    rawArguments(list(
        "--processor-module-path", "deps",     // enable JMH annotation processor
        "--default-module-for-created-files", "com.github.forax.beautifullogger.perf"
    ))

runner.
  modulePath(path("target/main/exploded", "deps")).
  module("com.github.forax.beautifullogger.tool/com.github.forax.beautifullogger.tool.Rewriter").
  mainArguments(list("target/main/exploded/com.github.forax.beautifullogger"))
  
docer.
  quiet(true).
  link(uri("https://docs.oracle.com/javase/9/docs/api/"))

packager.
    moduleMetadata(list(
        "com.github.forax.beautifullogger@0.9.7",
        "com.github.forax.beautifullogger.tool@0.9.7",
        "com.github.forax.beautifullogger.perf@0.9.7",
        "com.github.forax.beautifullogger.integration.log4j@0.9.7",
        "com.github.forax.beautifullogger.integration.slf4jj@0.9.7"
    ))

run(resolver, modulefixer, compiler, runner, tester, docer, packager)

pro.arguments().forEach(plugin -> run(plugin))   // run command line defined plugins


/exit

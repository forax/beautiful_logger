import static com.github.forax.pro.Pro.*;
import static com.github.forax.pro.builder.Builders.*;

packager.
    moduleMetadata(list(
        "com.github.forax.beautifullogger@0.9.5",
        "com.github.forax.beautifullogger.tool@0.9.5"
    ))

docer.
  rawArguments(list("-link", "https://docs.oracle.com/javase/9/docs/api/"))

resolver.
    dependencies(list(
        "org.objectweb.asm=org.ow2.asm:asm:6.0",
        
        "org.junit.jupiter.api=org.junit.jupiter:junit-jupiter-api:5.1.0",
        "org.junit.jupiter.params=org.junit.jupiter:junit-jupiter-params:5.1.0",
        "org.junit.platform.commons=org.junit.platform:junit-platform-commons:1.1.0",
        "org.apiguardian.api=org.apiguardian:apiguardian-api:1.0.0",
        "org.opentest4j=org.opentest4j:opentest4j:1.0.0"
    ))

run(resolver, compiler, tester, docer, packager)

/exit

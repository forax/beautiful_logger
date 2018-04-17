# beautiful_logger [![](https://api.travis-ci.org/forax/beautiful_logger.svg?branch=master)](https://travis-ci.org/forax/beautiful_logger)
Yet another logger API in Java with beautiful features

beautiful_logger is a mostly-zero-overhead wrapper on top of the existing logging implementations with a familiar API (info, error, etc)
that let you configure/re-configure the logger dynamically in a programmatic way.

This library requires Java 8 and is fully compatible with Java 9 modules.

The javadoc of the latest version is [available online](https://jitpack.io/com/github/forax/beautiful_logger/master-SNAPSHOT/javadoc/).

## Features
- *real* zero cost (no allocation, no branch, no assembly code) if a logger is disabled
- zero overhead cost when delegating to the logging libraries you already use, SLF4J, Log4J, Logback, JUL or SystemLogger [JEP 264](http://openjdk.java.net/jeps/264).
- allow user defined Log services to higher up your logging practice
- dynamic configuration/re-configuration which doesn't use costly inter-thread signaling
- no configuration file, no XML, etc, everything is done programmatically
- very small modular jar with no dependency


## Why another logging API ?

Technically it's more a facade like SLF4J, anyway, beautiful_logger exists because no other existing logging libraries
provide at least one of features listed above. 


## Why do you claim that there is no overhead ?

The implementation of this API ensures that the JIT can fully inline any calls to the Logger API without decrementing your inlining budget.
This is similar to the way, MethodHandle or VarHandle are optimized in the JDK.
The main drawback is that it put more pressure to the JITs so it may lengthen the time to steady state of an application.


## Example

```java
import com.github.forax.beautifullogger.Logger;

class Example {
  // getLogger with no argument uses the current class as configuration class
  private static final Logger LOGGER = Logger.getLogger();
  
  public static void main(String[] args) {
    for(int i = 0; i < 10; i++) {
      // use a lambda that does not capture any parameters 
      LOGGER.error((int value) -> "message " + value, i);
      
      if (i == 1) {
        // disable the logger programmatically
        LoggerConfig.fromClass(Example.class).update(opt -> opt.enable(false));
      }
    }
  }
}
```

## Build Tool Integration [![](https://jitpack.io/v/forax/beautiful_logger.svg)](https://jitpack.io/#forax/beautiful_logger)

Get latest binary distribution via [JitPack](https://jitpack.io/#forax/beautiful_logger) 


### Maven

    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    <dependency>
        <groupId>com.github.forax</groupId>
        <artifactId>beautiful_logger</artifactId>
        <version>master-SNAPSHOT</version>
    </dependency>


### Gradle

    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        compile 'com.github.forax:beautiful_logger:master-SNAPSHOT'
    }

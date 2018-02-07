# beautiful_logger
Yet another logger API in Java with beautiful features

Technically it's not a new logging API but more a wrapper (like SLF4J) around any logging libraries you want to use.
With Java 9 comes the new shiny Java System.Logger API ([JEP 264](http://openjdk.java.net/jeps/264)) that allow
to plug any library (any LoggerFinder) as a backend.
This API is a mostly-zero-overhead wrapper on top the System.Logger API with a familiar (info, error, etc)
that let you configure/re-configure the logger dynamically in a programmatic way.

This library required Java 9 and obviously is fully Java 9 compatible.


## Features
- *real* zero cost (no allocation, no branch, no assembly code) if a logger is disabled
- zero overhead cost when delegating to the logging libraries you already use (Log4J, Logback, etc)
- allow user defined Log services to higher up your logging practice
- dynamic configuration/re-configuration without using slow dependency injection libs
- no configuration file, no XML, etc, everything is done programmatically
- very small modular jar


## Why another logging API ?

Because no other existing logging libraries provide at least one of features listed above. 


## Why do you claim that there is no overhead ?

The implementation of this API ensure that the JIT can fully inline any calls to the Logger API without decrementing your inlining budget.
This is similar to the way, MethodHandle or VarHandle are optimized in the JDK.
The main drawbacks is that it put more pressure to the JITs so it may lengthen the time to steady state of an application.


## Build Tool Integration

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

# beautiful_logger
Yet another logger API in Java with beautiful features

Technically it's not a new logging API but more a wrapper (more like SLF4J) around any logging libraries you want to use.
By default, it delegates to the Java System.Logger API and you can configure the logging library you want to use
on any Loggers, packages or modules.

This library required Java 9 and obviously is fully Java 9 compatible.

## Features
- *real* zero cost (no allocation, no branch, no assembly code) disabled logger
- zero overhead cost when delegating to the logging libraries you already use (Log4J, Logback, etc)
- allow user defined Log services to higher up your logging practice
- dynamic configuration/re-configuration without using slow dependency injection libs
- no configuration file, no XML, etc, everything is done programmatically
- very small modular jar

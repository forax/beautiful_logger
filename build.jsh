//usr/bin/env jshell --show-version --execution local "$0" "$@"; exit $?

/*
 * Define global properties.
 */
String name = "com.github.forax.beautifullogger"

String junitPlatformVersion = "1.1.0-M2" // bundles the corresponding Jupiter "5.x.y[-z]"

Path sources = Paths.get("src/main/java")
Path tests = Paths.get("src/test/java")

Path classes = Paths.get("bin/bach/classes")
Path javadoc = Paths.get("bin/bach/javadoc")

Path beautifulJar = Paths.get("bin/bach/beautifullogger.jar")
Path beautifulSources = Paths.get("bin/bach/beautifullogger-sources.jar")
Path beautifulJavadoc = Paths.get("bin/bach/beautifullogger-javadoc.jar")

/*
 * Switch Bach to verbose mode. That'll print the commands before execution.
 */
System.setProperty("bach.verbose", "true")

/*
 * Download "Bach.java" and "Bach.jsh" from github to local binary directory.
 */
Path target = Files.createDirectories(Paths.get("bin/bach"))
URL context = new URL("https://raw.githubusercontent.com/sormuras/bach/1.0.0/src/bach/")
for (Path script : Set.of(target.resolve("Bach.java"), target.resolve("Bach.jsh"))) {
    if (Files.exists(script)) continue; // comment to force download files
    try (InputStream stream = new URL(context, script.getFileName().toString()).openStream()) {
        Files.copy(stream, script, StandardCopyOption.REPLACE_EXISTING);
    }
}

/*
 * Source "Bach.java" and "Bach.jsh" into this jshell session.
 */
/open bin/bach/Bach.java
/open bin/bach/Bach.jsh

java("--version")

/*
 * Compile.
 *
 * Compile using "--module-source-path" and "--module" combo fails:
 * <pre>
 *   javac("-d", classes.resolve("main"), "--module-source-path", sources, "--module", name)
 *   error: module com.github.forax.beautifullogger not found in module source path
 * </pre>
 * So, we have to simulate "$(find src -name '*.java')" using a self-expanding visitor.
 */
javac("-d", classes.resolve("main"), Bach.Command.visit(command -> command.addAllJavaFiles(sources)))


/*
 * Javadoc.
 */
javadoc("-html5", "-quiet", "-Xdoclint:all,-missing", "-linksource", "-link", "https://docs.oracle.com/javase/9/docs/api", "-d", javadoc, Bach.Command.visit(command -> command.addAllJavaFiles(sources)))

/*
 * Package.
 */
jar("--create", "--file", beautifulJar, "-C", classes.resolve("main"), ".")
jar("--create", "--file", beautifulSources, "-C", sources, ".")
jar("--create", "--file", beautifulJavadoc, "-C", javadoc, ".")

/*
 * Load and use JUnit Platform Console Standalone distribution for compiling and running tests.
 */
Path junit = new Bach.Basics.Resolvable("org.junit.platform", "junit-platform-console-standalone", junitPlatformVersion).resolve(Paths.get("bin/bach/tools/junit"), Bach.Basics.Resolvable.REPOSITORIES)

String testClassPath = String.join(File.pathSeparator, beautifulJar.toString(), junit.toString())
javac("-d", classes.resolve("test"), "--class-path", testClassPath, Bach.Command.visit(command -> command.addAllJavaFiles(tests)))

java("-ea", "-jar", junit, "--class-path", classes.resolve("test"), "--class-path", beautifulJar, "--scan-classpath");

/exit

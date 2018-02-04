//usr/bin/env jshell --show-version --execution local "$0" "$@"; exit $?

/*
 * Download "Bach.java" and "Bach.jsh" from github to local binary directory.
 */
Path target = Files.createDirectories(Paths.get("bin/bach/download"))
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
/open bin/bach/download/Bach.java
/open bin/bach/download/Bach.jsh

java("--version")

/*
 * Source "Build.java"
 */
/open src/bach/Build.java

/*
 * Build the project.
 */
var error = 0
try {
  Build.main();
} catch (Throwable throwable) {
  throwable.printStackTrace();
  error = 1;
}

/exit error

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

interface Build {

	Path SOURCE_MAIN = Paths.get("src", "main", "java");
	Path SOURCE_TEST = Paths.get("src", "test", "java");

	Path MODULES = Paths.get("bin", "bach", "modules");
	Path TARGET = Paths.get("bin", "bach", "target");
	Path TARGET_MAIN = TARGET.resolve("classes/main");
	Path TARGET_TEST = TARGET.resolve("classes/test");
	Path JAVADOC = TARGET.resolve("javadoc");
	Path ARTIFACTS = TARGET.resolve("artifacts");


	static void main(String... args) throws Exception {
		System.out.printf("%n[build]%n%n");
		resolveRequiredModules();

		Bach.log.level = Bach.Log.Level.VERBOSE;
		compileMain();
		javadoc();
		jar();
		jdeps();
		compileTest();
		test();
	}

	static void resolveRequiredModules() {
		// "JUnit 5"
		var jupiterVersion = "5.1.0-M2";
		var platformVersion = "1.1.0-M2";
		resolve("org.junit.jupiter", "junit-jupiter-api", jupiterVersion);
		resolve("org.junit.jupiter", "junit-jupiter-engine", jupiterVersion);
		resolve("org.junit.jupiter", "junit-jupiter-params", jupiterVersion);
		resolve("org.junit.platform", "junit-platform-console", platformVersion);
		resolve("org.junit.platform", "junit-platform-commons", platformVersion);
		resolve("org.junit.platform", "junit-platform-engine", platformVersion);
		resolve("org.junit.platform", "junit-platform-launcher", platformVersion);
		// 3rd-party modules
		resolve("org.opentest4j", "opentest4j", "1.0.0");
		resolve("org.apiguardian", "apiguardian-api", "1.0.0");
	}

	static Path resolve(String group, String artifact, String version) {
		return new Bach.Basics.Resolvable(group, artifact, version).resolve(MODULES, Bach.Basics.Resolvable.REPOSITORIES);
	}

	static void compileMain() {
		System.out.printf("%n[main][compile]%n%n");
		var javac = new Bach.JdkTool.Javac();
		javac.generateAllDebuggingInformation = true;
		javac.destination = TARGET_MAIN;
		javac.moduleSourcePath = List.of(SOURCE_MAIN);
		javac.modulePath = List.of(MODULES);
		javac.run();
	}

	static void compileTest() {
		System.out.printf("%n[test][compile]%n%n");
		var javac = new Bach.JdkTool.Javac();
		javac.destination = TARGET_TEST;
		javac.moduleSourcePath = List.of(SOURCE_TEST);
		javac.modulePath = List.of(MODULES);
		javac.patchModule = Bach.Basics.getPatchMap(List.of(SOURCE_TEST), List.of(SOURCE_MAIN));
		javac.run();
	}

	static void test() {
		System.out.printf("%n[test]%n%n");
		var java = new Bach.JdkTool.Java();
		java.modulePath = List.of(TARGET_TEST, MODULES);
		java.addModules = List.of("ALL-MODULE-PATH,ALL-DEFAULT");
		java.module = "org.junit.platform.console";
		java.args = List.of("--scan-modules");
		java.run();
	}

	static void javadoc() throws Exception {
		System.out.printf("%n[javadoc]%n%n");
		Files.createDirectories(JAVADOC);
		Bach.run("javadoc",
				"-html5",
				"-quiet",
				"-Xdoclint:all,-missing",
				"-package",
				"-linksource",
				"-link",
				"https://docs.oracle.com/javase/9/docs/api",
				"-d",
				JAVADOC,
				"-sourcepath",
				SOURCE_MAIN.resolve("com.github.forax.beautifullogger"),
				//"--module-source-path",
				//SOURCE_MAIN,
				"-subpackages",
				"com");
	}

	static void jar() throws Exception {
		System.out.printf("%n[jar]%n%n");
		Files.createDirectories(ARTIFACTS);
		jar("beautiful-logger.jar", TARGET_MAIN);
		jar("beautiful-logger-sources.jar", SOURCE_MAIN);
		jar("beautiful-logger-javadoc.jar", JAVADOC);
	}

	private static void jar(String artifact, Path path) {
		var jar = new Bach.JdkTool.Jar();
		jar.file = ARTIFACTS.resolve(artifact);
		jar.path = path;
		jar.run();
	}

	static void jdeps() {
		System.out.printf("%n[jdeps]%n%n");

		var jdeps = new Bach.JdkTool.Jdeps();
		jdeps.summary = true;
		jdeps.recursive = true;
		jdeps.toCommand().add(ARTIFACTS.resolve("beautiful-logger.jar")).run();
	}
}

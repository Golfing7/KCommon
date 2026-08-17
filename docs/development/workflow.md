# Development workflow

## Build KCommon

Run the repository's Gradle build with JDK 21. Published bytecode targets Java
8; modules that require Java 21 APIs use the Java 21 toolchain with a Java 8
release target:

```shell
./gradlew jar
```

Run the full build when you need the assembled artifacts:

```shell
./gradlew build
```

To consume a local build from another Gradle project:

```shell
./gradlew :APIPackaging:publishToMavenLocal
```

KCommon does not currently provide a JUnit test suite for plugin behavior.
Test modules in a development Bukkit/Paper server with the required runtime
dependencies installed.

## Test a module reload

1. Build and install the plugin jars in the server's `plugins` directory.
2. Start the server and confirm the module appears in `/km list`.
3. Change a module configuration value.
4. Run `/km reload <module>`.
5. Verify the module's commands, listeners, menus, and placeholders reflect
   the new state.

Module reloads are intended to replace a plugin reload during development.
Avoid keeping module-owned listeners, scheduled tasks, commands, or cached
objects in static fields.

## GitHub Pages documentation

The repository's `mkdocs.yml` defines the documentation site. Preview it
locally with:

```shell
python -m pip install -r requirements.txt
mkdocs serve
```

The GitHub Actions workflow at `.github/workflows/docs.yml` builds the site in
strict mode for pull requests and deploys the `main` branch to GitHub Pages.
In the repository settings, set **Pages > Build and deployment > Source** to
**GitHub Actions** once, then pushes to `main` publish the site.

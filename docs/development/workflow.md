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

## Check KCommon's status

`/kstatus` (alias `/ks`) prints an overview of KCommon: versions, uptime and
memory, the Mongo and redis connections, and how many plugins, modules,
commands, data managers, placeholders, and open menus are registered. The
details are split into sub commands:

```text
/kstatus plugins                    # Each KCommon plugin and what it registered
/kstatus modules [plugin] [-v]      # Module states, flags modules that failed to enable
/kstatus commands [plugin] [-v]     # Registered commands, their aliases and sub commands
/kstatus data [plugin] [-s] [-v]    # Data managers and their cache sizes
/kstatus connections [-v]           # Mongo and redis connection details (alias: db)
```

`-v` adds detail such as dependencies, permissions, and storage locations.
`/kstatus data -s` also counts what each manager has in storage, which reads the
disk or database off of the server thread. `/kstatus connections` pings Mongo
and redis to confirm they answer.

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

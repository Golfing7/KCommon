# Prerequisites and dependency

## Prerequisites

Run this repository's Gradle build with **JDK 21**, as required by the project
build. The KCommon subprojects compile with a Java 8 toolchain, so a plugin that
consumes the API can target the Java version supported by its server range.

KCommon's runtime plugin also declares these Bukkit dependencies:

| Plugin | Required by KCommon |
| --- | --- |
| PlaceholderAPI | Yes |
| Vault | Yes |
| HolographicDisplays | Optional |
| DecentHolograms | Optional |
| ItemsAdder | Optional |

Install the required plugins on the development server before testing a
plugin that depends on KCommon.

## Gradle setup

KCommon publishes the API under `com.golfing8:KCommon`. For a local checkout,
publish it to the local Maven repository first:

```shell
./gradlew :APIPackaging:publishToMavenLocal
```

Then add KCommon as a compile-time-only dependency. KCommon is supplied by the
server as a plugin, so it should not normally be bundled into your plugin jar.

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://maven.mckore.com/releases")
    }
    maven {
        url = uri("https://maven.mckore.com/snapshots")
    }
}

dependencies {
    compileOnly("com.golfing8:KCommon:1.2-SNAPSHOT")
}
```

Use the released KCommon version that matches your server in a published
plugin project. Keep `mavenLocal()` while developing against a locally built
version.

## `plugin.yml`

Declare KCommon as a hard dependency so Bukkit loads it before your plugin:

```yaml
name: ExamplePlugin
main: com.example.example.ExamplePlugin
version: 1.0.0
api-version: "1.13"
depend:
  - KCommon
```

The `main` class must extend `KPlugin`. Do not copy KCommon's own
`plugin.yml`; it is the runtime plugin descriptor for KCommon itself.

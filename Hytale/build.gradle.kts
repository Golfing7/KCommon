// KCommon for Hytale servers. Shares everything platform independent with Common.
repositories {
    mavenCentral()
    maven { url = uri("https://maven.hytale.com/release/") }
    maven { url = uri("https://repo.extendedclip.com/releases/") }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

val hytaleServerVersion = "0.7.4"

dependencies {
    implementation(project(":Common"))

    compileOnly("com.hypixel.hytale:Server:$hytaleServerVersion")
    compileOnly("at.helpch:placeholderapi-hytale:1.0.8")
    compileOnly(files(rootDir.resolve("locallibs/OrbisGuard-0.10.1.jar")))

    // Hytale ships Gson, fastutil and BSON, but none of these (its com.google.common only holds flogger).
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.google.guava:guava:33.4.8-jre")
    implementation(rootProject.libs.adventure.libraries)
    implementation("net.kyori:adventure-text-serializer-legacy:${rootProject.libs.versions.adventure.libraries.get()}")
    implementation(rootProject.libs.exp4j)
    implementation(rootProject.libs.expiringmap)
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.mongodb:mongodb-driver-sync:5.6.2") {
        exclude(group = "org.mongodb", module = "bson")
    }
    implementation(rootProject.libs.jedis)
}

tasks.processResources {
    filesMatching("manifest.json") {
        expand("version" to rootProject.version)
    }
}

// Platform independent core of KCommon. Nothing in here may touch Bukkit or Hytale.
dependencies {
    compileOnly("com.google.guava:guava:17.0")
    compileOnly("com.google.code.gson:gson:2.2.4")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("it.unimi.dsi:fastutil:8.5.12")
    compileOnly(rootProject.libs.mongo.sync)
    compileOnly(rootProject.libs.jedis)
    compileOnly(rootProject.libs.adventure.libraries)
    compileOnly("net.kyori:adventure-text-serializer-legacy:${rootProject.libs.versions.adventure.libraries.get()}")
    compileOnly(rootProject.libs.exp4j)
    compileOnly("org.apache.commons:commons-lang3:3.12.0")
    compileOnly("org.yaml:snakeyaml:2.2")

    testImplementation("org.yaml:snakeyaml:2.2")
    testImplementation("com.google.guava:guava:17.0")
    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation("it.unimi.dsi:fastutil:8.5.12")
    testImplementation(rootProject.libs.junit.jupiter.api)
    testRuntimeOnly(rootProject.libs.junit.jupiter.engine)
    testRuntimeOnly(rootProject.libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

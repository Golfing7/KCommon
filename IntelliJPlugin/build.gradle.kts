plugins {
    id("java")
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.golfing8"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2024.2")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.plugins.yaml")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.test {
    useJUnitPlatform()
}

intellijPlatform {
    pluginConfiguration {
        name = "KCommon Config YAML Support"
        ideaVersion {
            sinceBuild = "242"
        }
    }
}

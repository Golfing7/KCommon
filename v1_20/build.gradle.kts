plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "com.golfing8"
version = "1.0"

repositories {
    mavenCentral()

    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly(project(":KCommon"))
    compileOnly(project(":NMS"))
    compileOnly("com.tcoded:FoliaLib:0.5.2")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.4") {
        exclude("org.spigotmc")
    }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.15") {
        exclude("org.spigotmc")
    }
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

configurations.configureEach {
    if (isCanBeResolved) {
        attributes {
            attribute(
                org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE,
                21
            )
        }
    }
    if (isCanBeConsumed) {
        attributes {
            attribute(
                org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE,
                8
            )
        }
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
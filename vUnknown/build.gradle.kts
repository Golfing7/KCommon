plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
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
    testImplementation(project(":KCommon"))
    testImplementation("org.apache.commons:commons-lang3:3.14.0")
    testImplementation("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    compileOnly(project(":NMS"))
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.4") {
        exclude("org.spigotmc")
    }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.15") {
        exclude("org.spigotmc")
    }
    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.toSetOrEmpty

plugins {
    id("java")
    id("com.gradleup.shadow") version ("8.3.6")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"
    id("net.kyori.blossom") version "2.2.0"
    id("maven-publish")
    kotlin("jvm")
}

repositories {
    mavenCentral()

    maven {
        name = "PlaceholderAPI"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
    }

    maven {
        name = "matteodev"
        url = uri("https://maven.devs.beer/")
    }

    maven {
        name = "Lumine"
        url = uri("https://mvn.lumine.io/repository/maven-public/")
    }
}

tasks {
    shadowJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

dependencies {
    testImplementation("net.techcable.tacospigot:WineSpigot:1.8.8-R0.2-SNAPSHOT")
    testImplementation(rootProject.libs.junit.jupiter.api)
    testRuntimeOnly(rootProject.libs.junit.jupiter.engine)
    testRuntimeOnly(rootProject.libs.adventure.platform)
    testImplementation(kotlin("test"))
    testImplementation(project(":NMS"))
    testImplementation(rootProject.libs.mongo.sync)
    testImplementation(rootProject.libs.xseries)

    compileOnly("net.techcable.tacospigot:WineSpigot:1.8.8-R0.2-SNAPSHOT")
    compileOnly(rootProject.libs.exp4j)
    compileOnly(rootProject.libs.xseries)
    compileOnly(rootProject.libs.mongo.sync)
    compileOnly(rootProject.libs.mythicmobs)
    implementation(rootProject.libs.itemnbtapi)
    implementation("me.lucko:jar-relocator:1.7")

    compileOnly(project(":NMS"))
    compileOnly("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.0")
    compileOnly("com.github.decentsoftware-eu.decentholograms:decentholograms:2.9.5")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") //Vault
    compileOnly("dev.lone:api-itemsadder:4.0.10")
}

sourceSets {
    main {
        blossom {
            javaSources {
                setOf(
                    rootProject.libs.versions.xseries.get() to "xseries",
                    rootProject.libs.versions.mongo.get() to "mongo",
                    rootProject.libs.versions.exp4j.get() to "exp4j",
                    rootProject.libs.versions.expiringmap.get() to "expiringmap",
                    rootProject.libs.versions.adventure.platform.get() to "adventureplatform",
                    rootProject.libs.versions.adventure.libraries.get() to "adventurelibraries",
                    rootProject.libs.versions.itemnbtapi.get() to "itemnbtapi",
                    rootProject.version.toString() to "kcommon"
                ).forEach {
                    property("version_${it.second}", it.first)
                }
            }
            resources {
                property("version_kcommon", rootProject.version.toString())
            }
        }
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
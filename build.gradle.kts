plugins {
    id("java")
    id("com.gradleup.shadow") version("8.3.6")
    id("maven-publish")
}

val libraryFolder = "locallibs"
val commonsVersion = "1.1"

group = "com.golfing8"
version = commonsVersion

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()

    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }

    flatDir {
        dir(rootDir.resolve(libraryFolder))
    }
}

tasks {
    build {
        finalizedBy(shadowJar)
    }

    jar {
        manifest {
            attributes(
                "paperweight-mappings-namespace" to "mojang"
            )
        }
    }

    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")
        relocate("de.tr7zw.changeme.nbtapi", "de.tr7zw.kcommon.nbtapi")
        relocate("com.cryptomorin.xseries", "com.golfing8.shade.com.cryptomorin.xseries")
        relocate("org.objectweb.asm", "com.golfing8.shade.org.objectweb.asm")
        exclude("kotlin-*.jar")
    }

    publishToMavenLocal {
        dependsOn(build)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))

        withSourcesJar()
    }

    dependencies {
        annotationProcessor("org.projectlombok:lombok:1.18.36")
        compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
        compileOnly("net.kyori:adventure-platform-bukkit:4.3.3")
        compileOnly("net.jodah:expiringmap:0.5.11")
        compileOnly("org.projectlombok:lombok:1.18.36")
        compileOnly("de.tr7zw:item-nbt-api:2.15.2-SNAPSHOT") //For items.
        compileOnly("org.jetbrains:annotations:24.1.0")
    }

    repositories {
        mavenCentral()

        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        maven {
            name = "CodeMC"
            url = uri("https://repo.codemc.org/repository/maven-public/")
        }

        flatDir {
            dir(rootDir.resolve(libraryFolder))
        }
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")

    implementation(project(":KCommon"))
    implementation(project(":NMS"))
    implementation(project(":v1_8"))
    implementation(project(":vUnknown"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
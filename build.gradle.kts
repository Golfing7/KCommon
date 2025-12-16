plugins {
    id("java")
    id("com.gradleup.shadow") version ("8.3.6")
    id("maven-publish")
    checkstyle
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
    apply(plugin = "checkstyle")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))

        withSourcesJar()
    }

    dependencies {
        annotationProcessor(rootProject.libs.lombok)
        implementation(rootProject.libs.adventure.libraries)
        implementation(rootProject.libs.adventure.platform)
        compileOnly(rootProject.libs.expiringmap)
        compileOnly(rootProject.libs.lombok)
        compileOnly(rootProject.libs.itemnbtapi)
        compileOnly(rootProject.libs.annotations)
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

    checkstyle {
        configDirectory = rootDir.resolve(".checkstyle")
    }
}

dependencies {
    implementation(project(":KCommon"))
    implementation(project(":NMS"))
    implementation(project(":v1_8"))
    implementation(project(":vUnknown"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
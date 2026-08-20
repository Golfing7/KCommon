plugins {
    id("java")
    id("com.gradleup.shadow") version ("9.3.0")
    id("maven-publish")
    id ("java-library")
    checkstyle
}

val libraryFolder = "locallibs"
val commonsVersion = "1.3-SNAPSHOT"

group = "com.golfing8"
version = commonsVersion

repositories {
    mavenCentral()

    maven {
        name = "koredevReleases"
        url = uri("https://maven.mckore.com/releases")
        metadataSources {
            mavenPom()
            artifact() // Tells Gradle it's okay to fallback directly to the .jar
        }
    }

    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
    maven {
        name = "tcoded-releases"
        url = uri("https://repo.tcoded.com/releases")
    }

    flatDir {
        dir(rootDir.resolve(libraryFolder))
    }
}

tasks {
    build {
        dependsOn(shadowJar)
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
        relocate("com.tcoded.folialib", "com.golfing8.shade.com.tcoded.folialib")
        relocate("org.objectweb.asm", "com.golfing8.shade.org.objectweb.asm")
        relocate("org.bstats", project.group.toString())
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
    pluginManager.apply("java")
    pluginManager.apply("checkstyle")
    pluginManager.apply("java-library")

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

        maven {
            name = "koredevReleases"
            url = uri("https://maven.mckore.com/releases")
            metadataSources {
                mavenPom()
                artifact() // Tells Gradle it's okay to fallback directly to the .jar
            }
        }

        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        maven {
            name = "CodeMC"
            url = uri("https://repo.codemc.org/repository/maven-public/")
        }
        maven {
            name = "tcoded-releases"
            url = uri("https://repo.tcoded.com/releases")
        }

        flatDir {
            dir(rootDir.resolve(libraryFolder))
        }
    }

    checkstyle {
        configDirectory = rootDir.resolve(".checkstyle")
        toolVersion = "8.40"
    }
}

dependencies {
    implementation(project(":KCommon"))
    implementation(project(":NMS"))
    implementation(project(":v1_8"))
    implementation(project(":v1_20"))
    implementation(project(":v26_1"))
    implementation(project(":DialogMenus"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
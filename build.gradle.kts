plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version("7.1.2")
    id("maven-publish")
}

val libraryFolder = "locallibs"
val commonsVersion = "1.0"

group = "com.golfing8"
version = commonsVersion

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
    mavenLocal()

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
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")
        relocate("de.tr7zw.changeme.nbtapi", "de.tr7zw.kcommon.nbtapi")
    }

    publishToMavenLocal {
        dependsOn(build)
    }
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])

                groupId = project.group as String
                version = project.version as String
                artifactId = project.name
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.golfing8"
            artifactId = "KCommon"
            version = commonsVersion
            artifact(file("build/libs/${project.name}-${project.version}.jar"))
            pom {
                packaging = "jar"
            }
        }
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    }

    dependencies {
        annotationProcessor("org.projectlombok:lombok:1.18.24")
        compileOnly("org.projectlombok:lombok:1.18.24")
        compileOnly("de.tr7zw:item-nbt-api:2.12.1") //For items.
    }

    repositories {
        mavenCentral()
        mavenLocal()

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
    implementation(project(":v1_19"))
    implementation(project(":v1_17"))
    implementation(project(":v1_8"))
    implementation(project(":vUnknown"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
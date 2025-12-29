plugins {
    id("java")
    id("com.gradleup.shadow") version ("8.3.6")
    id("maven-publish")
}

val artifactName = parent!!.name

group = parent!!.group
version = parent!!.version

repositories {
    mavenCentral()
}

tasks {
    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")
        relocate("de.tr7zw.changeme.nbtapi", "de.tr7zw.kcommon.nbtapi")
        relocate("com.cryptomorin.xseries", "com.golfing8.shade.com.cryptomorin.xseries")
        relocate("org.objectweb.asm", "com.golfing8.shade.org.objectweb.asm")
    }

    build {
        dependsOn("shadowJar")
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

dependencies {
    implementation(parent!!)

    implementation(rootProject.libs.itemnbtapi)
    implementation(rootProject.libs.exp4j)
    implementation(rootProject.libs.expiringmap)

    implementation(rootProject.libs.adventure.platform)
    implementation(rootProject.libs.adventure.libraries)
    implementation(rootProject.libs.xseries) {
        isTransitive = false
    }

    implementation(rootProject.libs.mongo.sync)
}

// Javadoc module detection requires project evaluation (so api module is detected)
gradle.projectsEvaluated {
    // -------------------------------------------------- //
    //                      Javadocs                      //
    // -------------------------------------------------- //
    // Take api, core
    //   The version specific implementation modules don't have public API or javadocs
    //   They are excluded to avoid Javadoc errors due to NMS references that javadoc can't handle
    val exportedProjects = listOf(
        project(":KCommon"),
        project(":NMS"),
    )

    val aggregateJavadoc = tasks.register<Javadoc>("aggregateJavadoc") {
        val javaProjects = exportedProjects.filter { project ->
            project.plugins.hasPlugin("java")
        }

        // println("Generating Javadocs for projects (${javaProjects.size}): ${javaProjects.map { it.path }}")

        source(javaProjects.map { proj ->
            proj.extensions.getByType<SourceSetContainer>()["main"].allJava.matching {
                // Exclude classes that Javadoc can't handle, and that aren't needed in the docs
                exclude("**/WorldEdit6.java")
                exclude("**/WorldGuard6.java")
                exclude("**/WorldEdit7.java")
                exclude("**/WorldGuard7.java")
            }
        })
        classpath = files(javaProjects.map {
            it.extensions.getByType<SourceSetContainer>()["main"].compileClasspath
        })

        setDestinationDir(file("${layout.buildDirectory.get().asFile.absolutePath}/docs/aggregateJavadoc"))

        (options as StandardJavadocDocletOptions).apply {
            encoding = "UTF-8"
            charSet = "UTF-8"

            // Set window title and doc title
            windowTitle = "KCommon"
            docTitle = "KCommon ${rootProject.version} API"
        }
    }

    // Create the Javadoc JAR task (provides rich javadocs in IDEs)
    val aggregateJavadocJar = tasks.register<Jar>("aggregateJavadocJar") {
        group = "documentation"
        description = "Assembles a JAR archive containing the combined Javadocs"

        archiveClassifier.set("javadoc")
        from(aggregateJavadoc.get().destinationDir)

        dependsOn(aggregateJavadoc)
    }

    // Create the combined sources JAR (contains .java files) (provides fallback sources in IDEs)
    val aggregateSourcesJar = tasks.register<Jar>("aggregateSourcesJar") {
        group = "build"
        description = "Assembles sources JAR for all modules"

        val javaProjects = exportedProjects.filter {
            it.plugins.hasPlugin("java")
        }

        from(javaProjects.map {
            it.extensions.getByType<SourceSetContainer>()["main"].allSource
        })
        archiveClassifier.set("sources")
    }


    // -------------------------------------------------- //
    //                     publishing                     //
    // -------------------------------------------------- //
    tasks.publish.get().dependsOn(aggregateJavadocJar)
    tasks.publish.get().dependsOn(aggregateSourcesJar)
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = artifactName
                version = project.version.toString()

                from(components["shadow"])
                // Add both documentation artifacts
                artifact(tasks.named("aggregateJavadocJar")) // HTML documentation
                artifact(tasks.named("aggregateSourcesJar")) // Java source files
                artifact(tasks.named("shadowJar")) {
                    classifier = null
                }
            }
        }
    }
}
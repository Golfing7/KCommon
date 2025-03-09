plugins {
    id("java")
    id("com.gradleup.shadow") version("8.3.6")
    id("maven-publish")
}

group = "com.golfing8"
version = "1.0"

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

    implementation("de.tr7zw:item-nbt-api:2.14.0") //For items.
    implementation("net.objecthunter:exp4j:0.4.8") //For evaluating expressions.
    implementation("net.jodah:expiringmap:0.5.11")

    implementation("net.kyori:adventure-platform-bukkit:4.3.3")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("com.github.cryptomorin:XSeries:11.3.0") //For XSeries
    implementation("org.mongodb:mongodb-driver-sync:5.0.1")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.golfing8"
            artifactId = "KCommon"
            version = "1.0"

            from(components["java"])
        }
    }
}
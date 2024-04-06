plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version("7.1.2")
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

    implementation("de.tr7zw:item-nbt-api:2.12.3") //For items.
    implementation("net.objecthunter:exp4j:0.4.8") //For evaluating expressions.
    implementation("com.github.cryptomorin:XSeries:9.8.1") //For XSeries
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
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
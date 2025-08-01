import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("com.gradleup.shadow") version("8.3.6")
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
}

tasks {
    shadowJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

dependencies {
    testImplementation("net.techcable.tacospigot:WineSpigot:1.8.8-R0.2-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testRuntimeOnly("net.kyori:adventure-platform-bukkit:4.3.3")
    testImplementation(kotlin("test"))
    testImplementation(project(":NMS"))
    testImplementation("org.mongodb:mongodb-driver-sync:5.0.1")
    testImplementation("com.github.cryptomorin:XSeries:13.3.3")

    compileOnly("net.techcable.tacospigot:WineSpigot:1.8.8-R0.2-SNAPSHOT")
    compileOnly("net.objecthunter:exp4j:0.4.8") //For evaluating expressions.
    compileOnly("com.github.cryptomorin:XSeries:13.3.3") //For XSeries
    compileOnly("org.mongodb:mongodb-driver-sync:5.0.1")
    implementation("de.tr7zw:item-nbt-api:2.15.2-SNAPSHOT") //For items.
    implementation("me.lucko:jar-relocator:1.7")

    compileOnly(project(":NMS"))
    compileOnly("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.0")
    compileOnly("com.github.decentsoftware-eu.decentholograms:decentholograms:2.9.5")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") //Vault
    compileOnly("dev.lone:api-itemsadder:4.0.10")
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
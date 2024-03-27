plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version("7.1.2")
    id("maven-publish")
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
}

tasks {
    shadowJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("io.papermc:paper-api:1.12.2")
    testImplementation(project(":NMS"))

    implementation("de.tr7zw:item-nbt-api:2.12.1") //For items.
    implementation("net.objecthunter:exp4j:0.4.8") //For evaluating expressions.
    implementation("com.github.cryptomorin:XSeries:9.8.1") { isTransitive = false } //For XSeries
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")

    compileOnly(project(":NMS"))
    compileOnly("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.0")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.7.7")
    compileOnly("me.clip:placeholderapi:2.11.2")
    compileOnly("io.papermc:paper-api:1.12.2")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") //Vault
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
plugins {
    id("java")
}

group = "com.golfing8"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":NMS"))
    compileOnly("com.sk89q.worldguard:worldguard:6.x")
    compileOnly("com.sk89q.worldedit:FAWE:19.11")
    compileOnly("com.sk89q.worldedit:WorldEdit:6.x")
    compileOnly("net.techcable.tacospigot:WineSpigot:1.8.8-R0.2-SNAPSHOT")
    compileOnly("com.mojang:authlib:1.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
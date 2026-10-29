plugins {
    id("java")
}

repositories {

}

dependencies {
    api(project(":Common"))
    compileOnly("com.mojang:authlib:1.5.21")
    compileOnly("io.papermc.paper:paper-api:1.12.2")
}
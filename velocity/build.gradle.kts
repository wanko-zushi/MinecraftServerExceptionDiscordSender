plugins {
    id("net.kyori.blossom") version "1.3.1"
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
}

tasks {
    blossom {
        replaceToken("{name}", rootProject.name, "src/main/java/dev/s7a/mseds/velocity/MinecraftServerExceptionDiscordSenderPlugin.java")
        replaceToken("{version}", rootProject.version.toString(), "src/main/java/dev/s7a/mseds/velocity/MinecraftServerExceptionDiscordSenderPlugin.java")
    }
}

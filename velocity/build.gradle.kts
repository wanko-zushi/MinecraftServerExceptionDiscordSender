import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask.JarUrl

plugins {
    alias(libs.plugins.blossom)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)
    compileOnly(libs.config.toml)
}

tasks {
    blossom {
        replaceToken("{name}", rootProject.name, "src/main/java/dev/s7a/mseds/velocity/MinecraftServerExceptionDiscordSenderPlugin.java")
        replaceToken("{version}", rootProject.version.toString(), "src/main/java/dev/s7a/mseds/velocity/MinecraftServerExceptionDiscordSenderPlugin.java")
    }
}

task<LaunchMinecraftServerTask>("testPlugin") {
    dependsOn("build")

    doFirst {
        copy {
            from(buildDir.resolve("libs/MinecraftServerExceptionDiscordSender.jar"))
            into(buildDir.resolve("MinecraftServer/plugins"))
        }
    }

    jarUrl.set(JarUrl.Velocity("3.1.1"))
}

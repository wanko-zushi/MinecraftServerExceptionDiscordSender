import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask.JarUrl

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

task<LaunchMinecraftServerTask>("testPlugin") {
    dependsOn("build")

    doFirst {
        copy {
            from(buildDir.resolve("libs/${project.name}.jar"))
            into(buildDir.resolve("MinecraftServer/plugins"))
        }
    }

    jarUrl.set(JarUrl.Velocity("3.1.1"))
}

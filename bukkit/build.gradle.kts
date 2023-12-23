import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask.JarUrl
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    alias(libs.plugins.plugin.yml.bukkit)
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly(libs.spigot.api)
}

configure<BukkitPluginDescription> {
    name = rootProject.name
    main = "dev.s7a.mseds.bukkit.MinecraftServerExceptionDiscordSenderPlugin"
    version = rootProject.version.toString()
    author = "sya-ri"
    apiVersion = "1.13"
}

task<LaunchMinecraftServerTask>("testPlugin") {
    dependsOn("build")

    doFirst {
        copy {
            from(buildDir.resolve("libs/MinecraftServerExceptionDiscordSender.jar"))
            into(buildDir.resolve("MinecraftServer/plugins"))
        }
    }

    jarUrl.set(JarUrl.Paper(libs.versions.paper.get()))
    agreeEula.set(true)
}

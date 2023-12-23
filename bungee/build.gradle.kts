import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask.JarUrl
import net.minecrell.pluginyml.bungee.BungeePluginDescription

plugins {
    alias(libs.plugins.plugin.yml.bungee)
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly(libs.bungeecord.api)
}

configure<BungeePluginDescription> {
    name = rootProject.name
    main = "dev.s7a.mseds.bungee.MinecraftServerExceptionDiscordSenderPlugin"
    version = rootProject.version.toString()
    author = "sya-ri"
}

task<LaunchMinecraftServerTask>("testPlugin") {
    dependsOn("build")
    dependsOn(project(":bungee-test").tasks.getByName("build"))

    doFirst {
        copy {
            from(buildDir.resolve("libs/MinecraftServerExceptionDiscordSender-bungee.jar"))
            into(buildDir.resolve("MinecraftServer/plugins"))
        }
        copy {
            from(project(":bungee-test").buildDir.resolve("libs/ExceptionTest.jar"))
            into(buildDir.resolve("MinecraftServer/plugins"))
        }
    }

    jarUrl.set(JarUrl.Waterfall(libs.versions.waterfall.get()))
}

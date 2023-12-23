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
    name = "ExceptionTest"
    main = "dev.s7a.mseds.bukkit.test.ExceptionTestPlugin"
    version = rootProject.version.toString()
    author = "sya-ri"
    apiVersion = "1.13"
}

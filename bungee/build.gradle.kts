import net.minecrell.pluginyml.bungee.BungeePluginDescription

plugins {
    id("net.minecrell.plugin-yml.bungee") version "0.5.2"
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")
}

configure<BungeePluginDescription> {
    name = rootProject.name
    main = "dev.s7a.mseds.bungee.MinecraftServerExceptionDiscordSenderPlugin"
    version = rootProject.version.toString()
    author = "sya-ri"
}

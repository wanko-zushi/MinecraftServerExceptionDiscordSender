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
    name = "ExceptionTest"
    main = "dev.s7a.mseds.bungee.test.ExceptionTestPlugin"
    version = rootProject.version.toString()
    author = "sya-ri"
}

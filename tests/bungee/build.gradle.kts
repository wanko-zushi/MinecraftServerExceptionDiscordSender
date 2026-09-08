import net.minecrell.pluginyml.bungee.BungeePluginDescription

plugins {
    alias(libs.plugins.plugin.yml.bungee)
}

dependencies {
    compileOnly(libs.bungeecord.api) {
        exclude(group = "net.md-5", module = "brigadier")
    }
}

configure<BungeePluginDescription> {
    name = "ExceptionTest"
    main = "dev.s7a.mseds.bungee.test.ExceptionTestPlugin"
    version = rootProject.version.toString()
    author = "sya-ri"
}

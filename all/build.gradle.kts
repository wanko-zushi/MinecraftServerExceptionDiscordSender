import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask.*

dependencies {
    implementation(project(":bukkit"))
    implementation(project(":bungee"))
    implementation(project(":velocity"))
}

tasks.withType<ShadowJar> {
    archiveFileName.set("MinecraftServerExceptionDiscordSender.jar")
}

listOf(
    "Bukkit" to JarUrl.Paper(libs.versions.paper.get()),
    "Bungee" to JarUrl.Waterfall(libs.versions.waterfall.get()),
    "Velocity" to JarUrl.Velocity(libs.versions.velocity.server.get())
).forEach { (name, url) ->
    task<LaunchMinecraftServerTask>("testPlugin$name") {
        dependsOn("build")
        dependsOn(project(":${name.lowercase()}-test").tasks.getByName("build"))

        doFirst {
            copy {
                from(buildDir.resolve("libs/MinecraftServerExceptionDiscordSender.jar"))
                into(buildDir.resolve("MinecraftServer$name/plugins"))
            }
            copy {
                from(project(":${name.lowercase()}-test").buildDir.resolve("libs/ExceptionTest.jar"))
                into(buildDir.resolve("MinecraftServer$name/plugins"))
            }
        }

        serverDirectory.set(buildDir.resolve("MinecraftServer$name").toString())
        jarUrl.set(url)
        agreeEula.set(true)
    }
}

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

subprojects {
    afterEvaluate {
        apply(plugin = libs.plugins.minecraft.server.get().pluginId)
    }

    dependencies {
        implementation(project(":common"))
    }

    tasks.withType<ShadowJar> {
        archiveFileName.set("MinecraftServerExceptionDiscordSender-${project.name}.jar")
    }
}

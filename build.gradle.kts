import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    id("dev.s7a.gradle.minecraft.server") version "2.0.0" apply false
}

group = "dev.s7a"
version = "1.0.0-SNAPSHOT"

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:23.0.0")
    }
}

subprojects {
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "dev.s7a.gradle.minecraft.server")

    dependencies {
        implementation(project(":"))
    }

    tasks.withType<ShadowJar> {
        archiveFileName.set("MinecraftServerExceptionDiscordSender.jar")
    }

    tasks.build {
        dependsOn("shadowJar")
    }
}

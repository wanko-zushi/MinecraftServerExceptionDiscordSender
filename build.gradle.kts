import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.minecraft.server) apply false
}

group = "dev.s7a"
version = "1.0.0-SNAPSHOT"

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    afterEvaluate {
        dependencies {
            compileOnly(libs.annotations)
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

subprojects {
    afterEvaluate {
        apply(plugin = libs.plugins.shadow.get().pluginId)
        apply(plugin = libs.plugins.minecraft.server.get().pluginId)
    }

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

dependencies {
    compileOnly("org.apache.logging.log4j:log4j-core:2.19.0")
    compileOnly("com.google.code.gson:gson:2.10")
}

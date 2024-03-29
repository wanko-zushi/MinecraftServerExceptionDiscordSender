import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask.JarUrl

plugins {
    alias(libs.plugins.blossom)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("name", rootProject.name)
                property("version", rootProject.version.toString())
            }
        }
    }
}

task<LaunchMinecraftServerTask>("testPlugin") {
    dependsOn("build")
    dependsOn(project(":tests:velocity").tasks.getByName("build"))

    doFirst {
        copy {
            from(buildDir.resolve("libs/MinecraftServerExceptionDiscordSender-velocity.jar"))
            into(buildDir.resolve("MinecraftServer/plugins"))
        }
        copy {
            from(project(":tests:velocity").buildDir.resolve("libs/ExceptionTest.jar"))
            into(buildDir.resolve("MinecraftServer/plugins"))
        }
    }

    jarUrl.set(JarUrl.Velocity(libs.versions.velocity.server.get()))
}

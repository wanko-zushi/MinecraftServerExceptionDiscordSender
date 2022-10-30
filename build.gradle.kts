plugins {
    id("java")
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
    apply(plugin = "dev.s7a.gradle.minecraft.server")

    dependencies {
        implementation(project(":"))
    }
}

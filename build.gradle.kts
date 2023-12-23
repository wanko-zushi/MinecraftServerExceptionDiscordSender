plugins {
    id("java")
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.minecraft.server) apply false
    id("com.github.ben-manes.versions") version "0.41.0"
}

group = "dev.s7a"
version = "1.0.0-SNAPSHOT"

subprojects {
    apply(plugin = "java")
    afterEvaluate {
        apply(plugin = libs.plugins.shadow.get().pluginId)
    }

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

    tasks.build {
        dependsOn("shadowJar")
    }
}

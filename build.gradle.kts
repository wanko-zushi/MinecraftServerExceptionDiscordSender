plugins {
    id("java")
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
    dependencies {
        implementation(project(":"))
    }
}

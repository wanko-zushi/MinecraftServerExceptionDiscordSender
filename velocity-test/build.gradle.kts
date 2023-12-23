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
                property("name", "ExceptionTest")
                property("version", rootProject.version.toString())
            }
        }
    }
}

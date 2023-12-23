import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

subprojects {
    afterEvaluate {
        apply(plugin = libs.plugins.shadow.get().pluginId)
    }

    tasks.withType<ShadowJar> {
        archiveFileName.set("ExceptionTest.jar")
    }
}

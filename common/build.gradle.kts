dependencies {
    compileOnly(libs.log4j.core)
    compileOnly(libs.gson)
    testImplementation(libs.log4j.core)
    testImplementation(libs.gson)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

tasks.test {
    useJUnitPlatform()
}

plugins {
    id("java")
}

group = "me.hysong"
version = "1.0-SNAPSHOT"

// Ensure that all submodules are evaluated (required for multi-module builds)
evaluationDependsOnChildren()

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(files("Libraries3/JsonCoder.jar"))


}

tasks.test {
    useJUnitPlatform()
}

tasks.named("build") {
    dependsOn(
        subprojects.mapNotNull { sub ->
            // only include this subproject if it really has a 'build' task
            sub.tasks.findByName("build")?.let { sub.tasks.named("build") }
        }
    )
}

// --- OR, a dedicated 'buildAll' aggregator: ---
tasks.register("buildAll") {
    group = "build"
    description = "Assembles and tests all subprojects that actually have a build task"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("build")?.let { sub.tasks.named("build") }
        }
    )
}
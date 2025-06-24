plugins {
    id("java")
}

group = "me.hysong"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(project(":Libraries:KSFoundation"))

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    testCompileOnly("org.projectlombok:lombok:1.18.36")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
}

tasks.test {
    useJUnitPlatform()
}


tasks.jar {
    // 1) Name the archive after the project only (no version)
    archiveBaseName.set(project.name + ".sl")
    archiveVersion.set("")            // strips “-1.0.0”
    archiveClassifier.set("")         // ensures no “-all” or similar suffix

    // 2) Point the output directory at ../../Storage/Library/
    destinationDirectory.set(file("../../Storage/Library"))

    // 3) Optional: manifest if needed by consumers
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
}

// 4) Ensure `build` invokes our custom jar configuration (redundant—build→assemble→jar by default,
// but explicit wiring guarantees it)
tasks.named("build") {
    dependsOn(tasks.named("jar"))
}
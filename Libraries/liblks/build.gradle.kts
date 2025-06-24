plugins {
    id("java")
}

group = "me.hysong"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set(project.name + ".sl")
    archiveVersion.set("")
    archiveClassifier.set("")

    destinationDirectory.set(file("../../Storage/Library"))

    // 3) Optional: manifest if needed by consumers
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
}

tasks.named("build") {
    dependsOn(tasks.named("jar"))
}
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.hysong"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Project‑local SDKs
    implementation(project(":Libraries:KSFoundation"))
    implementation(project(":Libraries:KSTraderAPI"))
    implementation(project(":Libraries:liblks"))
    implementation(files("../../Libraries3/JsonCoder.jar"))

    // External libraries
    implementation("com.google.code.gson:gson:2.13.0")
    implementation("com.auth0:java-jwt:4.5.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Lombok (compile‑only + annotation processing)
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    testCompileOnly("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")
}

tasks.test {
    useJUnitPlatform()
}
//
//tasks.jar {
//    archiveBaseName.set(project.name + ".sl")
//    archiveVersion.set("")
//    archiveClassifier.set("")
//
//    destinationDirectory.set(file("../../Storage/Drivers"))
//
//    from({
//        configurations
//            .runtimeClasspath
//            .get()
//            .filter { it.name.endsWith(".jar") }
//            .map { zipTree(it) }
//    })
//
//    manifest {
//        attributes["Implementation-Title"] = project.name
//        attributes["Implementation-Version"] = project.version
//    }
//}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set(project.name + ".sl")
    archiveClassifier.set("") // no "-all" suffix
    archiveVersion.set("") // no version in name
    destinationDirectory.set(file("../../Storage/Drivers"))

    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }

    // Optional: exclude project-local jars if not needed inside fat jar
    dependencies {
        exclude(dependency(":Libraries:KSFoundation"))
        exclude(dependency(":Libraries:KSTraderAPI"))
        exclude(dependency(":Libraries:liblks"))
    }
}



tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}
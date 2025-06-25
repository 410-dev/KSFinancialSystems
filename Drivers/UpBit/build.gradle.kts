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

    // Project‑local SDKs
    implementation(project(":Libraries:KSFoundation"))
    implementation(project(":Libraries:KSTraderAPI"))
    implementation(project(":Libraries:liblks"))

    implementation("com.squareup.okhttp3:okhttp:4.12.0")


    implementation(files("../../Libraries3/JsonCoder.jar"))

    // External libraries
    implementation("com.google.code.gson:gson:2.13.0")
    implementation("com.auth0:java-jwt:4.5.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")

    // Lombok (compile‑only + annotation processing)
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    testCompileOnly("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set(project.name + ".sl")
    archiveVersion.set("")
    archiveClassifier.set("")

    destinationDirectory.set(file("../../Storage/Drivers"))

    // 3) Optional: manifest if needed by consumers
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
}

tasks.named("build") {
    dependsOn(tasks.named("jar"))
}
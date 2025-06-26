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
    implementation("org.eclipse.jetty:jetty-server:11.0.15")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.15")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0") // 6.x is Servlet 6.0; adjust if needed
    implementation("org.slf4j:slf4j-simple:2.0.7")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    // 1. Sets the Main-Class in the JAR's MANIFEST.MF:
    //    This tells the Java runtime where to start execution when you run `java -jar ...`
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Main-Class" to "kstradermachine.KSTraderMachine" // Crucial for executable JAR
        )
    }

    // 2. Set target directory for the fat JAR:
    archiveFileName.set(project.name + ".service.jar")
    archiveClassifier.set("")
    destinationDirectory.set(file("../../Storage/Services"))


    // 4. Includes the main project's compiled classes:
    from(sourceSets.main.get().output)


    // 5. Includes compiled classes only from subprojects starting with "_sdk":
    subprojects.forEach { subproj ->
        // Check if the subproject name starts with "Libraries"
        if (subproj.name.startsWith("Libraries")) {
            // Apply the rest of the logic only if the name matches
            subproj.pluginManager.withPlugin("java") {
                val sourceSets = subproj.extensions.findByType(SourceSetContainer::class.java)
                if (sourceSets != null) {
                    // Add the compiled output of the 'main' source set from this _sdk subproject
                    from(sourceSets.getByName("main").output)
                }
            }
        }
    }

    mergeServiceFiles()
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}

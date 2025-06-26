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

    implementation(project(":Libraries:liblks"))

    implementation("io.github.wuhewuhe:bybit-java-api:1.2.7")
    implementation("com.google.code.gson:gson:2.12.1")

    // Lombok, used only during compile time
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    testCompileOnly("org.projectlombok:lombok:1.18.36")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
}

tasks.test {
    useJUnitPlatform()
}
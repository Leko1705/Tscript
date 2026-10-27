plugins {
    application
    id("java")
}

group = "com.tscript"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("info.picocli:picocli:4.7.6")
    implementation("org.ow2.asm:asm:6.0")
    implementation("org.ow2.asm:asm-util:6.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.tscript.cli.tscript")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
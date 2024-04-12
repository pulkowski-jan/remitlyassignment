plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    application
}

group = "me.janpulkowski"
version = "1.0"


repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType(JavaExec::class.java) {
    standardInput = System.`in`
}

kotlin {
    jvmToolchain(11)
}


application {
    mainClass.set("me.janpulkowski.remitly.MainKt")
}

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This is a general purpose Gradle build.
 * Learn more about Gradle by exploring our Samples at https://docs.gradle.org/8.10/samples
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.24"
}

group = "com.t0xodile"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("net.portswigger.burp.extensions:montoya-api:2024.12")
}

tasks.jar {
    manifest {
        attributes(
                "Implementation-Title" to "Better CORS Scanner",
                "Implementation-Version" to version,
                "Burp-Extension-Name" to "Better CORS Scanner",
                "Burp-Extension-Version" to version,
                "Burp-Extension-Suite-Min-Version" to "2023.10.0",
                "Burp-Extension-Suite-Max-Version" to "2024.99.99"
        )
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

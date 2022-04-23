/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.4.2/userguide/building_java_projects.html
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.5.31"

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.apache.commons:commons-compress:1.21")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-base:${project.property("hapiFhirVersion")}")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-client:${project.property("hapiFhirVersion")}")

    testImplementation("ca.uhn.hapi.fhir:hapi-fhir-jaxrsserver-base:${project.property("hapiFhirVersion")}")

//	compile 'ca.uhn.hapi.fhir:hapi-fhir-structures-dstu2:${project.version}'
    implementation ("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:${project.property("hapiFhirVersion")}") // only needed for loading

}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest()
        }
    }
}

application {
    // Define the main class for the application.
    mainClass.set("io.floodplain.fhir.loader.AppKt")
}
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.71"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.apache.datasketches:datasketches-java:1.2.0-incubating")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.apache.commons:commons-csv:1.8")
    implementation("com.github.ajalt:clikt:2.6.0")
    implementation("com.amazonaws:aws-java-sdk-s3:1.11.758")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

sourceSets {
    main {
        java.srcDir("src/main/java")
        java.srcDir("src/main/resources")
        withConvention(KotlinSourceSet::class) { kotlin.srcDir("src/main/kotlin")}
    }

    test {
        java.srcDir("src/test/java")
        java.srcDir("src/test/kotlin")
    }
}



tasks.test {
    useJUnitPlatform()
    testLogging {
        testLogging.showStandardStreams = true
        events("passed", "skipped", "failed")
    }
}

// config JVM target to 1.8 for kotlin compilation tasks
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

plugins {
    `java-library`
    `maven-publish`
    signing
    id("com.diffplug.gradle.spotless") version ("3.28.1")
}

group = "com.whylabs"
version = "0.1-alpha"

repositories {
    mavenCentral()
}

spotless {
    java {
        googleJavaFormat()
    }
    format ("misc") {
        target("**/*.gradle", "**/*.md", "**/.gitignore")

        trimTrailingWhitespace()
        indentWithSpaces() // or spaces. Takes an integer argument if you don't like 4
        endWithNewline()
    }
}

dependencies {
    implementation("org.apache.datasketches:datasketches-java:1.2.0-incubating")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.apache.commons:commons-csv:1.8")
    implementation("com.esotericsoftware:kryo:5.0.0-RC5")

    // lombok support
    compileOnly("org.projectlombok:lombok:1.18.12")
    annotationProcessor("org.projectlombok:lombok:1.18.12")
    testCompileOnly("org.projectlombok:lombok:1.18.12")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.12")

    // testng
    testImplementation("org.testng:testng:6.8")
}

sourceSets {
    main {
        java.srcDir("src/main/java")
        java.srcDir("src/main/resources")
    }

    test {
        java.srcDir("src/test/java")
        java.srcDir("src/test/java")
    }
}

tasks.test {
    useTestNG()
    testLogging {
        testLogging.showStandardStreams = true
        failFast = true
        events("passed", "skipped", "failed")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "whylogs-java"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("WhyLogs")
                description.set("A lightweight data profiling library")
                url.set("http://www.whylogs.ai")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("WhyLabs")
                        name.set("WhyLabs, Inc")
                        email.set("info@whylabs.ai")
                    }
                }
                scm {
                    url.set("http://whylabs.ai/")
                }
            }
        }
    }
    repositories {
        maven {
            // change URLs to point to your repos, e.g. http://my.org/repo
            val releasesRepoUrl = uri("$buildDir/repos/releases")
            val snapshotsRepoUrl = uri("$buildDir/repos/snapshots")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

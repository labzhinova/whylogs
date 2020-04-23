import com.amazonaws.auth.DefaultAWSCredentialsProviderChain

plugins {
    `java-library`
    `maven-publish`
    signing
    id("com.diffplug.gradle.spotless") version ("3.28.1")
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.amazonaws:aws-java-sdk-core:1.11.766")
    }
}
group = "com.whylabs"
version = "0.1-alpha-SNAPSHOT"

repositories {
    mavenCentral()
}

spotless {
    java {
        googleJavaFormat()
    }
}


dependencies {
    implementation("org.apache.datasketches:datasketches-java:1.2.0-incubating")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.apache.commons:commons-csv:1.8")

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
            // TODO: change this URL base on the stage of publishing?
            val s3Base = "s3://whylabs-andy-maven-us-west-2/repos"
            val releasesRepoUrl = uri("$s3Base/releases")
            val snapshotsRepoUrl = uri("$s3Base/snapshots")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

            // set up AWS authentication
            val credentials = DefaultAWSCredentialsProviderChain.getInstance().credentials
            credentials(AwsCredentials::class) {
                accessKey = credentials.awsAccessKeyId
                secretKey = credentials.awsSecretKey
                // optional
                if (credentials is com.amazonaws.auth.AWSSessionCredentials) {
                    sessionToken = credentials.sessionToken
                }
            } }
    }
}

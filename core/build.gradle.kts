import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.util.Node
import groovy.util.NodeList

buildscript {
    dependencies {
        classpath("com.amazonaws:aws-java-sdk-core:1.11.766")
        classpath("com.github.jengelman.gradle.plugins:shadow:5.2.0")
    }
}


plugins {
    `java-library`
    `maven-publish`
    signing
    id("com.github.johnrengelman.shadow") version ("5.2.0")
}

group = "com.whylabs.logging"
version = "0.1-alpha"

spotless {
    java {
        googleJavaFormat()
    }
}

dependencies {
    api("org.slf4j:slf4j-api:1.7.27")
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
        java.srcDir("src/test/resources")
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

val shadowJar: ShadowJar by tasks
shadowJar.apply {
    archiveClassifier.set("bundle")
    exclude("*.properties")
    exclude("META-INF/*")
    dependencies {
        exclude(dependency("org.slf4j:slf4j-api:1.7.27"))
        exclude(dependency("org.apache.commons:commons-csv:1.8"))
    }
    relocate("org.apache.datasketches", "zzz.com.whylabs.org.apache.datasketches")
    relocate("com.google", "zzz.com.whylabs.com.google")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.whylabs"
            artifactId = "whylogs-java"
            version = version
            artifact(shadowJar)
            from(components["java"])
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

                // rewrite XML dependencies blob to only include SLF4J
                withXml {
                    val dependencies =
                        (asNode()["dependencies"] as NodeList)[0] as Node
                    val childNodes = dependencies.children().filterIsInstance<Node>()
                        .filter { (it.name() as groovy.xml.QName).qualifiedName == "dependency" }

                    // remove dependencies that are not slf4j
                    val dependenciesToBeRemoved = childNodes.filterNot {
                        val node =
                            (it.get("groupId") as NodeList)[0] as Node
                        val groupId = (node.value() as NodeList)[0] as String

                        groupId.startsWith("org.slf4j")
                    }

                    dependenciesToBeRemoved.forEach {
                        dependencies.remove(it)
                    }
                }
            }
        }

        repositories {
            val isSnapShot = version.toString().endsWith("SNAPSHOT")
            maven {
                // change URLs to point to your repos, e.g. http://my.org/repo
                val releasesRepoUrl = uri("$buildDir/repos/releases")
                val snapshotsRepoUrl = uri("$buildDir/repos/snapshots")
                url = if (isSnapShot) snapshotsRepoUrl else releasesRepoUrl

            }
            maven {
                // TODO: change this URL base on the stage of publishing?
                val s3Base = "s3://whylabs-andy-maven-us-west-2/repos"
                val releasesRepoUrl = uri("$s3Base/releases")
                val snapshotsRepoUrl = uri("$s3Base/snapshots")
                url = if (isSnapShot) snapshotsRepoUrl else releasesRepoUrl

                // set up AWS authentication
                val credentials = DefaultAWSCredentialsProviderChain.getInstance().credentials
                credentials(AwsCredentials::class) {
                    accessKey = credentials.awsAccessKeyId
                    secretKey = credentials.awsSecretKey
                    // optional
                    if (credentials is com.amazonaws.auth.AWSSessionCredentials) {
                        sessionToken = credentials.sessionToken
                    }
                }
            }
        }
    }
}


import com.amazonaws.auth.DefaultAWSCredentialsProviderChain

buildscript {
    dependencies {
        classpath("com.amazonaws:aws-java-sdk-core:1.11.766")
    }
}


plugins {
    scala
    `java-library`
    `maven-publish`
    id("com.github.maiflai.scalatest") version "0.26"
}

repositories {
    maven {
        // TODO: change this URL base on the stage of publishing?
        val s3Base = "s3://whylabs-andy-maven-us-west-2/repos"
        url = uri("$s3Base/releases")


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
    mavenLocal()
    jcenter()
}

group = "com.whylabs.logging.spark"
version = "0.1.2-alpha-${project.properties.getOrDefault("versionType", "SNAPSHOT")}"

spotless {
    java {
        googleJavaFormat()
    }
}

val scalaVersion = project.properties.getOrDefault("scalaVersion", "2.11")
val sparkVersion = "2.4.5"

fun scalaPackage(groupId: String, name: String, version: String) =
    "$groupId:${name}_$scalaVersion:$version"

dependencies {
    api("org.slf4j:slf4j-api:1.7.27")
    implementation(scalaPackage("org.apache.spark", "spark-core", sparkVersion))
    implementation(scalaPackage("org.apache.spark", "spark-sql", sparkVersion))

    // project dependencies
    implementation(project(":core"))
    // implementation("com.whylabs", "whylogs-java", "0.1.2-alpha-May192020", classifier="bundle")

    // lombok support
    compileOnly("org.projectlombok:lombok:1.18.12")
    annotationProcessor("org.projectlombok:lombok:1.18.12")
    testCompileOnly("org.projectlombok:lombok:1.18.12")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.12")

    // testng
    testImplementation("org.testng:testng:6.8")
    testImplementation(scalaPackage("org.scalatest", "scalatest", "3.1.2"))
    testRuntimeOnly("org.slf4j:slf4j-log4j12:1.7.30")
    testRuntimeOnly("com.vladsch.flexmark:flexmark-profile-pegdown:0.36.8")
}

tasks.test {
    useTestNG()
    jvmArgs("-Dlog4j.configuration=file://${projectDir}/configurations/log4j.properties")
    testLogging {
        testLogging.showStandardStreams = true
        failFast = true
        events("passed", "skipped", "failed")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.whylabs"
            artifactId = "whylogs-spark_$scalaVersion"
            version = version
            from(components["java"])
            pom {
                name.set("WhyLogs")
                description.set("Spark integration for WhyLogs")
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
                        (asNode()["dependencies"] as groovy.util.NodeList)[0] as groovy.util.Node
                    val childNodes = dependencies.children().filterIsInstance<groovy.util.Node>()
                        .filter { (it.name() as groovy.xml.QName).qualifiedName == "dependency" }

                    // remove dependencies that are not slf4j
                    val dependenciesToBeRemoved = childNodes.filterNot {
                        val node =
                            (it.get("groupId") as groovy.util.NodeList)[0] as groovy.util.Node
                        val groupId = (node.value() as groovy.util.NodeList)[0] as String

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


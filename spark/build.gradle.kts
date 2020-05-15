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
version = "0.1-alpha"

spotless {
    java {
        googleJavaFormat()
    }
}

val scalaVersion = "2.11"
val sparkVersion = "2.4.5"

fun scalaPackage(groupId: String, name: String, version: String) =
    "$groupId:${name}_$scalaVersion:$version"

dependencies {
    api("org.slf4j:slf4j-api:1.7.27")
    implementation(scalaPackage("org.apache.spark", "spark-core", sparkVersion))
    implementation(scalaPackage("org.apache.spark", "spark-sql", sparkVersion))

    // project dependencies
    implementation(project(":core"))
//    implementation("com.whylabs", "whylogs-java", "0.1-alpha", classifier="bundle")

    // lombok support
    compileOnly("org.projectlombok:lombok:1.18.12")
    annotationProcessor("org.projectlombok:lombok:1.18.12")
    testCompileOnly("org.projectlombok:lombok:1.18.12")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.12")

    // testng
    testImplementation("org.testng:testng:6.8")
    testImplementation(scalaPackage("org.scalatest", "scalatest", "3.1.2"))
    testRuntimeOnly("com.vladsch.flexmark:flexmark-profile-pegdown:0.36.8")
}

tasks.test {
    useTestNG()
    testLogging {
        testLogging.showStandardStreams = true
        failFast = true
        events("passed", "skipped", "failed")
    }
}

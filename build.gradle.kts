plugins {
    `java-library`

    id("com.diffplug.gradle.spotless") version ("3.28.1") apply false
}

group = "com.whylabs"
version = "0.2.0-alpha-${project.properties.getOrDefault("versionType", "SNAPSHOT")}"

if (rootProject.hasProperty("isGitLabCi")) {
    println("Running in GitLab. Skip setting hooks")
} else {
    println("Running outside of GitLab CI. Config Git hooks")
    project.exec {
        commandLine = "git config core.hooksPath hooks".split(" ")
    }
}

allprojects {
    apply(plugin = "java")
    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

subprojects {
    version = "1.0"
    apply(plugin = "com.diffplug.gradle.spotless")
}
//
//project(":whylogs-java:core") {
//    dependencies {
//        implementation(project(":proto"))
//    }
//}

//project(":whylogs-java:firehose") {
//    dependencies {
//        implementation(project(":proto"))
//    }
//}
//

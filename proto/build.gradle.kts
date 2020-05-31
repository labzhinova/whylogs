import com.google.protobuf.gradle.*

buildscript {
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.12")
    }
}

apply(plugin = "com.google.protobuf")

plugins {
    `java-library`
    idea
}

group = "com.whylabs"
version = rootProject.version

sourceSets {
    main {
        proto {
            srcDir("src")
        }
    }
}

dependencies {
    api("com.google.protobuf:protobuf-java:3.11.4")
}


protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:3.12.2"
    }
    plugins {
        // Optional: an artifact spec for a protoc plugin, with "grpc" as
        // the identifier, which can be referred to in the "plugins"
        // container of the "generateProtoTasks" closure.
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.15.1"
        }
    }

    generateProtoTasks {

        all().forEach { task ->
            run {
                task.builtins.create("python") {
//                    outputSubDir = "$projectDir/python"
                }
            }
        }
    }
}


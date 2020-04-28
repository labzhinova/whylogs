plugins {
    id("com.diffplug.gradle.spotless") version ("3.28.1") apply false
}

group = "com.whylabs"
version = "0.1-alpha"

allprojects {
    apply(plugin = "java")
    repositories {
        mavenCentral()
    }
}


subprojects {
    version = "1.0"
    apply(plugin = "com.diffplug.gradle.spotless")
}

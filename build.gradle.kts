plugins {
    `java-library`
    id("com.diffplug.gradle.spotless") version ("3.28.1")
}

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

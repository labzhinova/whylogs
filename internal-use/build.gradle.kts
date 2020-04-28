plugins {
    `java-library`
    `maven-publish`
    signing
    id("com.github.johnrengelman.shadow") version ("5.2.0")
}

group = "com.whylabs.internal"
version = "1.0"

spotless {
    java {
        googleJavaFormat()
    }
}

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.27")
    implementation(project(":core"))
    implementation("org.apache.commons:commons-csv:1.8")
    implementation("com.google.code.gson:gson:2.8.6")

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

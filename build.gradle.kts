plugins {
    java
    application
    id("com.diffplug.spotless") version "6.25.0"
}

group = "com.upcomingmovies"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("com.upcomingmovies.Main")
    applicationName = "upcoming-movies"
}

repositories {
    mavenCentral()
}

dependencies {
    // Selenium 4 with built-in Selenium Manager for headless Chrome
    implementation("org.seleniumhq.selenium:selenium-java:4.20.0")

    // CLI parser
    implementation("info.picocli:picocli:4.7.5")
    annotationProcessor("info.picocli:picocli-codegen:4.7.5")

    // Terminal interactive prompt & styling
    implementation("org.jline:jline:3.26.1")

    // iCalendar (.ics) RFC 5545 generator
    implementation("net.sf.biweekly:biweekly:0.6.8")

    // JSON serialization with Java 8/17 Date/Time and Optional support
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.17.0")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.13")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

spotless {
    java {
        googleJavaFormat()
    }
}

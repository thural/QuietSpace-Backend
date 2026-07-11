plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
}

group = "dev.thural"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

val mapstructVersion = "1.6.3"
val jjwtVersion = "0.13.0"
val springDocVersion = "3.0.3"
val thumbnailatorVersion = "0.4.20"
val lombokVersion = "1.18.46"
val springwolfVersion = "2.5.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.security:spring-security-messaging")

    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    implementation("org.mapstruct:mapstruct:$mapstructVersion")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")
    implementation("io.github.springwolf:springwolf-stomp:$springwolfVersion")
    implementation("io.github.springwolf:springwolf-ui:$springwolfVersion")
    runtimeOnly("io.github.springwolf:springwolf-stomp-binding:$springwolfVersion")
    implementation("net.coobird:thumbnailator:$thumbnailatorVersion")

    implementation("com.fasterxml.jackson.core:jackson-core")

    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.h2database:h2")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    testAnnotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Testcontainers
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers:1.21.4")
    testImplementation("org.testcontainers:mysql:1.21.4")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")

    // WireMock
    testImplementation("org.wiremock:wiremock-standalone:3.9.1")

    runtimeOnly("org.springframework.boot:spring-boot-devtools")
}

tasks.withType<Test> {
    useJUnitPlatform()
    environment("DOCKER_API_VERSION", "1.40")
}

val integrationTest by tasks.registering(Test::class) {
    description = "Runs integration tests only."
    group = "verification"

    useJUnitPlatform()

    filter {
        includeTestsMatching("*IT")
        includeTestsMatching("*ITCase")
    }

    shouldRunAfter(tasks.named("test"))
}

tasks.named("check") {
    dependsOn(integrationTest)
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Amapstruct.defaultComponentModel=spring")
}

springBoot {
    mainClass.set("dev.thural.quietspace.QuietspaceApplication")
}

tasks.jar {
    enabled = false
}

tasks.bootJar {
    layered {
        enabled = true
    }
    archiveFileName.set("quietspace-${project.version}.jar")
}

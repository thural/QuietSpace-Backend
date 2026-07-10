FROM gradle:9.6.1-jdk25-alpine AS build

WORKDIR /build

COPY gradlew gradlew.bat ./
COPY gradle ./gradle

RUN ./gradlew --version

COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

RUN ./gradlew clean bootJar -x test



FROM eclipse-temurin:25-jre-alpine AS builder

WORKDIR extracted

COPY --from=build /build/build/libs/*.jar ./app.jar

RUN java -Djarmode=layertools -jar app.jar extract



FROM eclipse-temurin:25-jre-alpine

WORKDIR application

COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

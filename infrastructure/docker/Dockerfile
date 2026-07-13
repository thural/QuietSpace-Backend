FROM gradle:9.6.1-jdk25-alpine AS build

WORKDIR /build

COPY gradlew gradlew.bat ./
COPY gradle ./gradle

RUN ./gradlew --version

COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

RUN ./gradlew clean bootJar -x test

FROM eclipse-temurin:25-jre-alpine AS builder

WORKDIR /builder

COPY --from=build /build/build/libs/*.jar ./application.jar

RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM eclipse-temurin:25-jre-alpine

WORKDIR /application

COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]

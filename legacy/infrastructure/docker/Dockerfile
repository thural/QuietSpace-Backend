FROM maven:3.9.6-eclipse-temurin-17-alpine as build

WORKDIR ./build

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests



FROM eclipse-temurin:17-jre-alpine as builder

WORKDIR extracted

COPY --from=build /build/target/*.jar ./app.jar

RUN java -Djarmode=layertools -jar app.jar extract



FROM eclipse-temurin:17-jre-alpine

WORKDIR application

COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
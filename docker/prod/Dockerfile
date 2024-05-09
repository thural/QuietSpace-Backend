FROM maven:3.9.6-eclipse-temurin-17-alpine as build

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jre-alpine

ARG PROFILE=prod
ARG VERSION=0.0.1

WORKDIR /app
COPY --from=build /build/target/*SNAPSHOT.jar /app/

EXPOSE 8080

ENV DB_USER_PASSWORD=undefined
ENV DB_USER_USERNAME=undefined
ENV DB_PORT_NUMBER=undefined
ENV DB_NAME=undefined
ENV DB_URL=undefined
ENV DB_HOST_NAME=undefined
ENV APP_VERSION=${VERSION}
ENV ACTIVE_PROFILE=${PROFILE}

CMD java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Dspring.profiles.active=prod -Dspring.datasource.url=${DB_URL}  quietspace-${APP_VERSION}-SNAPSHOT.jar
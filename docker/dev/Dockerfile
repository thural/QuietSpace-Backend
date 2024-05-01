FROM openjdk:17

COPY target/*.jar quietspace-monolith.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "quietspace-monolith.jar"]
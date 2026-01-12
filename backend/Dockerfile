# Simple Dockerfile using local build
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/shopping-app-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 7070
ENTRYPOINT ["java", "-jar", "app.jar"]

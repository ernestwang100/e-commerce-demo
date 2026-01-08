# Stage 1: Build
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy the jar from the build stage
COPY --from=build /app/target/shopping-app-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 7070
ENTRYPOINT ["java", "-jar", "app.jar"]

# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build

# Copy and build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
COPY checkstyle.xml ./
RUN mvn clean package -DskipTests

# Debug to see the built JAR file
RUN ls -la /build/target

# Runtime stage
FROM amazoncorretto:21

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /build/target/backend-0.0.1-SNAPSHOT.jar /build/target/backend-0.0.1.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "/build/target/backend-0.0.1.jar"]
# Build stage
FROM openjdk:21-jdk-slim as builder

WORKDIR /app

# Copy maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:21-jre-slim

WORKDIR /app

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Copy jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

USER spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
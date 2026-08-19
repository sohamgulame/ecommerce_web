# =========================================================================
# Stage 1: Build & Package Spring Boot Application
# =========================================================================
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

# Install dos2unix to ensure Windows CRLF line endings on mvnw don't fail in Linux
RUN apk add --no-cache dos2unix

# Copy Maven wrapper and POM files first for layer caching
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN dos2unix mvnw && chmod +x mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build executable jar
COPY src src
RUN ./mvnw clean package -DskipTests

# =========================================================================
# Stage 2: Minimal Production Runtime
# =========================================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a secure, non-root user and group
RUN addgroup -S spring && adduser -S spring -G spring

# Copy compiled jar from builder stage
COPY --from=builder /workspace/target/*.jar app.jar
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

EXPOSE 8080

# Configure production-optimized JVM flags
ENTRYPOINT ["java", \
    "-XX:+UseG1GC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]

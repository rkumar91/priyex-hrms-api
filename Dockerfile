# ═══════════════════════════════════════════════════════
# Stage 1: Build the Spring Boot application
# ═══════════════════════════════════════════════════════
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ═══════════════════════════════════════════════════════
# Stage 2: Minimal Production Runtime
# ═══════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root system user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built jar from builder stage
COPY --from=builder /app/target/priyex-hrms-api-*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app
USER appuser

# Expose port (Render/Koyeb will set PORT environment variable dynamically)
ENV PORT=8080
EXPOSE 8080

# Tuned JVM parameters for free-tier hosting (512MB RAM limits)
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Xss512k -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT:-8080} -jar app.jar"]

# ========== BUILD STAGE ==========
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 1. Copy pom.xml first (for dependency caching)
COPY pom.xml .

# 2. Copy source code
COPY src ./src

# 3. Build the application
RUN mvn -B clean package -DskipTests

# ========== RUNTIME STAGE ==========
FROM eclipse-temurin:17-jre-jammy

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8090/actuator/health || exit 1

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
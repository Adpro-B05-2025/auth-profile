# Multi-stage build for optimized production image
FROM gradle:8.13-jdk21 AS build

# Set working directory
WORKDIR /app

# Copy gradle wrapper and configuration files first (for better layer caching)
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached layer if gradle files don't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the application (skip tests for faster build, run them in CI/CD)
RUN ./gradlew bootJar --no-daemon -x test

# Production stage with optimized JRE
FROM eclipse-temurin:21-jre-jammy

# Set metadata
LABEL maintainer="PandaCare Team" \
      description="Auth-Profile Service for PandaCare Application" \
      version="1.0.0" \
      service="auth-profile"

# Create application directory and user for security
RUN groupadd -r pandacare && useradd -r -g pandacare -s /bin/false pandacare

# Install required packages for monitoring and debugging
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    jq \
    dumb-init \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# Create application directory
WORKDIR /app

# Create logs directory
RUN mkdir -p /app/logs && chown -R pandacare:pandacare /app

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

# Set ownership of the application files
RUN chown -R pandacare:pandacare /app

# Switch to non-root user
USER pandacare

# Set JVM options optimized for containerized environment
ENV JAVA_OPTS="-server \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/app/logs/ \
               -Djava.security.egd=file:/dev/./urandom \
               -Djava.awt.headless=true \
               -Dfile.encoding=UTF-8 \
               -Duser.timezone=Asia/Jakarta"

# Application configuration
ENV SERVER_PORT=8081 \
    SPRING_PROFILES_ACTIVE=docker \
    MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,prometheus,metrics,info \
    MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=when_authorized \
    LOGGING_LEVEL_ROOT=INFO \
    LOGGING_FILE_NAME=/app/logs/auth-profile.log

# Health check with proper timeout and intervals
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

# Expose port
EXPOSE ${SERVER_PORT}

# Create volumes for logs and temporary files
VOLUME ["/app/logs", "/tmp"]

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Run the application with optimized startup
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
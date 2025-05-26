# This should be the content of /home/ec2-user/auth-profile/Dockerfile on EC2
# This is a RUNTIME Dockerfile, expecting a pre-built JAR.

FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="PandaCare Team" \
      description="Auth-Profile Service for PandaCare Application" \
      version="1.0.0" \
      service="auth-profile"

# ARG instruction to receive the JAR filename from docker-compose.yml build.args
# The default value matches what we've been using.
ARG APP_JAR="auth-profile.jar"

# Create a non-root user and group for better security
RUN groupadd -r pandacare && useradd -r -g pandacare -s /bin/false pandacare

# Install essential packages that might be needed (e.g., for health checks, debugging)
# dumb-init is a lightweight init system for containers, helps with signal handling.
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    jq \
    dumb-init \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# Set the working directory inside the container
WORKDIR /app

# Create logs directory and set ownership before copying application files
# This is where the application will write its logs if configured to do so.
RUN mkdir -p /app/logs && chown -R pandacare:pandacare /app

# Copy the pre-built application JAR into the container
# ${APP_JAR} will be replaced by the value from docker-compose.yml (e.g., "auth-profile.jar")
COPY ${APP_JAR} app.jar

# Set ownership of all application files to the non-root user
RUN chown -R pandacare:pandacare /app

# Switch to the non-root user
USER pandacare

# Set JVM options for optimal performance in a containerized environment
# Customize these based on your application's needs and available resources.
ENV JAVA_OPTS="-server \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/app/logs/oom_dump.hprof \
               -Djava.security.egd=file:/dev/./urandom \
               -Djava.awt.headless=true \
               -Dfile.encoding=UTF-8 \
               -Duser.timezone=Asia/Jakarta"

# Default environment variables for the application.
# These will be overridden by values from the .env file loaded by docker-compose.
ENV SERVER_PORT=8081 \
    SPRING_PROFILES_ACTIVE=docker \
    MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,prometheus,metrics,info \
    MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=when_authorized \
    LOGGING_LEVEL_ROOT=INFO \
    LOGGING_LEVEL_AUTH_PROFILE=DEBUG \
    LOGGING_FILE_NAME=/app/logs/auth-profile.log
# Health check for the application to ensure it starts correctly
# Uses SERVER_PORT which will be resolved from environment variables at runtime.
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

# Expose the application port (will be resolved from SERVER_PORT env var at runtime)
EXPOSE ${SERVER_PORT}

# Define mount points for persistent data (logs) and temporary files.
# The actual mounting to host paths or named volumes is done in docker-compose.yml.
VOLUME ["/app/logs", "/tmp"]

# Use dumb-init as the entrypoint to properly handle signals and reap zombie processes
ENTRYPOINT ["dumb-init", "--"]

# Command to run the application JAR
# 'exec' allows the Java process to become PID 1 under dumb-init, helping with signal handling.
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
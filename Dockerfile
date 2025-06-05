FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="PandaCare Team" \
      description="Auth-Profile Service for PandaCare Application" \
      version="1.0.0" \
      service="auth-profile"

ARG APP_JAR="auth-profile.jar"

RUN groupadd -r pandacare && useradd -r -g pandacare -s /bin/false pandacare

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    jq \
    dumb-init \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

WORKDIR /app

RUN mkdir -p /app/logs && chown -R pandacare:pandacare /app

COPY ${APP_JAR} app.jar

RUN chown -R pandacare:pandacare /app

USER pandacare

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

ENV SERVER_PORT=8081 \
    SPRING_PROFILES_ACTIVE=docker \
    MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,prometheus,metrics,info \
    MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=when_authorized \
    LOGGING_LEVEL_ROOT=INFO \
    LOGGING_FILE_NAME=/app/logs/auth-profile.log

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

EXPOSE ${SERVER_PORT}

VOLUME ["/app/logs", "/tmp"]

ENTRYPOINT ["dumb-init", "--"]

CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
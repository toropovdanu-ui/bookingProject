FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser

WORKDIR /app

COPY build/libs/app.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
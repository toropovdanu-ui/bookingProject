FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser

RUN mkdir -p /app
COPY build/libs/event-booking-0.0.1-SNAPSHOT.jar app/app.jar
RUN chown appuser:appgroup app/app.jar

USER appuser
WORKDIR /app

ENTRYPOINT ["java", "-jar", "app.jar"]
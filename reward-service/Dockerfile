FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/reward-service-0.0.1-SNAPSHOT-exec.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
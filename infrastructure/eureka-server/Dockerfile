FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Expose Service Discovery (Eureka Server) port (8761)
EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]

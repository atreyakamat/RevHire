FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Expose API Gateway port (8080)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

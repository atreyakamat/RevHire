FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Expose Config Server port (8800)
EXPOSE 8800

ENTRYPOINT ["java", "-jar", "app.jar"]

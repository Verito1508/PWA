FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copia el jar generado por Maven
COPY target/*.jar app.jar

# Spring Boot por defecto usa 8080
ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Dserver.port=${SERVER_PORT}", "-jar", "/app/app.jar"]

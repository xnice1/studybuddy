FROM maven:3.9.11-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests=false verify
RUN mvn -B -DskipTests=false package


FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
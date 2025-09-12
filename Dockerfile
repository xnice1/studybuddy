FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
FROM maven:3.9.6-eclipse-temurin-21-alpine as builder

WORKDIR /app
COPY pom.xml .

RUN mvn dependency:go-offline

COPY src/ ./src/
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/target/gateway-*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
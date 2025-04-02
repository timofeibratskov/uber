FROM gradle:8.7-jdk21-alpine AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

RUN ./gradlew dependencies

COPY src ./src

RUN ./gradlew build -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/rating-service-*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
# TODO: Не работает, не находится main класс-_-
FROM gradle:7.2-jdk11 AS build

WORKDIR /app

COPY build.gradle.kts /app/
COPY settings.gradle /app/
COPY gradle.properties /app/
COPY src /app/src

RUN gradle build -x test --no-daemon

FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

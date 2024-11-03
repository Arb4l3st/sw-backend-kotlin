FROM openjdk:8-jdk-alpine

RUN apk add --no-cache gradle
WORKDIR /app
COPY build.gradle settings.gradle gradle.properties ./
COPY src ./src
RUN gradle build

CMD ["java", "-jar", "build/libs/test-budget-0.0.2-SNAPSHOT.jar"]
FROM openjdk:8-jdk-alpine

RUN apk add --no-cache gradle
WORKDIR /app
COPY build.gradle settings.gradle gradle.properties ./
COPY src ./src
RUN gradle build

CMD ["java", "-jar", "build/libs/my-project-2.0-SNAPSHOT.jar"]
FROM openjdk:8
MAINTAINER "Alexander Myasnikov"
WORKDIR test-backend
COPY build/libs/*-all.jar test-backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "test-backend.jar"]

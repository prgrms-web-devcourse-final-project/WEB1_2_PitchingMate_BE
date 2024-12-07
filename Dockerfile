FROM eclipse-temurin:17-jdk-alpine

# Redis 설치
RUN apk add --no-cache redis

COPY ./build/libs/*SNAPSHOT.jar project.jar
ENTRYPOINT redis-server & java -jar project.jar
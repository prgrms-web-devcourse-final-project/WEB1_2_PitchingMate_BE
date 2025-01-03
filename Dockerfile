FROM eclipse-temurin:17-jdk-alpine

# Redis와 크롤링에 필요한 패키지 설치
RUN apk add --no-cache \
    redis \
    chromium \
    chromium-chromedriver \
    nss \
    freetype \
    harfbuzz

# Chrome 환경변수 설정
ENV CHROME_BIN=/usr/bin/chromium-browser

COPY ./build/libs/*SNAPSHOT.jar project.jar
ENTRYPOINT redis-server & java -jar project.jar
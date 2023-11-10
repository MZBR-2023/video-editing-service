FROM openjdk:11-jdk-slim-buster

RUN apt-get update && \
    apt-get install -y --fix-missing ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir /spring

WORKDIR /spring


COPY build/libs/video-editing-service-0.0.1-SNAPSHOT.jar /spring/app.jar

ENTRYPOINT ["java", "-jar", "/spring/app.jar"]

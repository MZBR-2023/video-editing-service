FROM openjdk:11-jdk-slim-buster

# ffmpeg 설치
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY ./build/libs/*.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]

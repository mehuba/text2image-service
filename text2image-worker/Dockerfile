FROM openjdk:8-jdk-slim
WORKDIR /app
COPY target/text2image-worker.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

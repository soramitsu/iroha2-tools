FROM openjdk:11-jre
WORKDIR /app
COPY ./build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:21-jre
RUN mkdir /app
ADD backend/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
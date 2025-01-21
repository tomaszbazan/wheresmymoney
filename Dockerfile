FROM eclipse-temurin:21-jre
RUN mkdir /app
ADD build/libs/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
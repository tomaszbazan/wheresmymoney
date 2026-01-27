FROM eclipse-temurin:21-jre
RUN mkdir /app
ADD build/libs/*-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
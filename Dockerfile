FROM openjdk:21-jdk-slim

WORKDIR /app

COPY build/libs/matilda-0.0.1-SNAPSHOT.jar /app/app.jar

RUN mkdir -p /app/logs

EXPOSE 9999

ENTRYPOINT ["nohup", "java", "-jar","-Dspring.profiles.active=dev", "app.jar"]

# 지금은 필요없지만..

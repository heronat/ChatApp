FROM openjdk:24-slim-bookworm

LABEL maintainer="Rayen<"
LABEL version="1.0.0"
LABEL description="Docker image for the server for Chat APP, built for SR03 Project P24"

VOLUME /tmp
EXPOSE 80

ARG JAR_FILE=target/Devoir2-1.0.0.jar

ADD ${JAR_FILE} app.jar


ENTRYPOINT ["java","-jar","/app.jar"]
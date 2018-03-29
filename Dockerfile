FROM openjdk:8-jre-slim

ARG JAR_FILE

ADD ${JAR_FILE} app.jar

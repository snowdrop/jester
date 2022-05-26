FROM openjdk:${JAVA_VERSION}
COPY ${RUNNER} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
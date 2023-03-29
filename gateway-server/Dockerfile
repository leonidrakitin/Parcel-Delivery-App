FROM gradle:latest
COPY ./build/libs/gateway-server-0.0.1-SNAPSHOT.jar opt/app/app.jar
ENTRYPOINT ["java","-jar","opt/app/app.jar"]
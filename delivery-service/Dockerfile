FROM gradle:latest
COPY build/libs/delivery-service-0.0.1-SNAPSHOT.jar opt/app/app.jar
ENTRYPOINT ["java","-jar","opt/app/app.jar"]
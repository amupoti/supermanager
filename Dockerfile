FROM maven:3.6.0-jdk-11-slim as build

WORKDIR /app
COPY . .

RUN mvn clean package

FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=build /app/supermanager-viewer/target/*.jar ./

EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/supermanager-viewer-1.0-SNAPSHOT.jar"]

FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN  mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/VenzolTechnologyApi-0.0.1-SNAPSHOT.jar VenzolTechnologyApi.jar
EXPOSE 8068
ENTRYPOINT ["java","-jar","VenzolTechnologyApi.jar"]
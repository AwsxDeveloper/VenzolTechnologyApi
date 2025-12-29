FROM maven:3.9-eclipse-temurin-25-alpine AS build
COPY . .
RUN  mvn clean package -DskipTests

FROM eclipse-temurin:25
COPY --from=build /target/VenzolTechnologyApi-0.0.1-SNAPSHOT.jar VenzolTechnologyApi.jar
EXPOSE 8068
ENTRYPOINT ["java","-jar","VenzolTechnologyApi.jar"]
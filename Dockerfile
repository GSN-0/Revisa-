FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY mvnw .
COPY .mvn ./.mvn
COPY pom.xml .
COPY src ./src

RUN chmod +x mvnw && ./mvnw -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/target/revisa-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

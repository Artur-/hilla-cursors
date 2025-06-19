# The "Build" stage. Copies the entire project into the container, into the /app/ folder, and builds it.
FROM eclipse-temurin:17 AS BUILD
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:resolve-plugins dependency:resolve
    
COPY . /app/
WORKDIR /app/
RUN --mount=type=cache,target=/root/.m2 ./mvnw package -Pproduction

FROM eclipse-temurin:17
COPY --from=BUILD /app/target/hilla-cursors-1.0-SNAPSHOT.jar /app/app.jar
WORKDIR /app/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

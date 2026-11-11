FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
ENV MAVEN_OPTS="-Djava.net.preferIPv4Stack=true"
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 chmod +x mvnw && ./mvnw -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/target/pact-backend-*.jar app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

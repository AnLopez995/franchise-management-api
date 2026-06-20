# --- Build stage: compile and package the app (tests run in CI, skipped here) ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Leverage layer caching: resolve dependencies before copying the source.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Runtime stage: slim JRE image with just the fat jar ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Run as an unprivileged user.
RUN useradd --system --no-create-home appuser
COPY --from=build /app/target/*.jar app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

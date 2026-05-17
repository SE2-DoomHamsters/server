# ── Stage 1: Build ────────────────────────────────────────────────────────────
# Uses a full Maven + JDK image to compile the project and produce a fat JAR.
# Dependencies are downloaded first (separate layer) so rebuilds are faster
# when only source code changes.
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# ── Stage 2: Run ──────────────────────────────────────────────────────────────
# Uses a lightweight JRE-only image — no Maven, no compiler, no source code.
# Only the compiled JAR from Stage 1 is copied over, keeping the final image small.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Document that the server listens on port 8080 inside the container.
# Mapped to the university server port 53217 via docker-compose.yml.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

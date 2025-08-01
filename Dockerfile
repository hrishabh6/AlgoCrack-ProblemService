# ---- Stage 1: Build with Gradle ----
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Copy only what's needed to build (optional: speeds up builds)
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY gradlew .
COPY gradle/ ./gradle/

# Run gradle once to download dependencies (layer caching)
RUN ./gradlew build --no-daemon || return 0

# Copy rest of the project
COPY . .

# Build the app (skip tests for speed)
RUN ./gradlew build --no-daemon -x test

# ---- Stage 2: Run the app ----
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port (optional)
EXPOSE 8080

# Run the app with Render-provided port
CMD ["sh", "-c", "java -jar app.jar --server.port=$PORT"]

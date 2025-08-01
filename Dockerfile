# ---- Stage 1: Build with Gradle ----
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Copy only what's present in your repo
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
COPY gradle/ ./gradle/

# Run Gradle once to cache dependencies
RUN ./gradlew build --no-daemon || return 0

# Copy the rest of the app
COPY . .

# Build the JAR (skipping tests for speed)
RUN ./gradlew build --no-daemon -x test

# ---- Stage 2: Run the app ----
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the generated JAR
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose app port (for docs)
EXPOSE 8080

# Run the JAR with Render's dynamic PORT
CMD ["sh", "-c", "java -jar app.jar --server.port=$PORT"]

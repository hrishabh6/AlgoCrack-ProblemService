# -- Stage 1: Build with OpenJDK 24 and Gradle installed manually --
FROM openjdk:24-jdk-bullseye AS builder

RUN apt-get update && apt-get install -y wget unzip && \
    wget https://services.gradle.org/distributions/gradle-8.5-bin.zip && \
    unzip gradle-8.5-bin.zip -d /opt && \
    ln -s /opt/gradle-8.5/bin/gradle /usr/bin/gradle

WORKDIR /app

# Copy all project files first
COPY build.gradle settings.gradle gradlew ./
COPY gradle/ ./gradle/
RUN chmod +x gradlew
COPY . .

# Now, run the Gradle build command
RUN ./gradlew build --no-daemon -x test

# -- Stage 2: Runtime using JDK 24 --
FROM openjdk:24-jdk-bullseye

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
CMD ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
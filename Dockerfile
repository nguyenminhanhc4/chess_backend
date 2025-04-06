# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy all files
COPY . .

# Install required tools for verification
RUN apt-get update && \
    apt-get install -y file && \
    rm -rf /var/lib/apt/lists/*

# Verify and set permissions for Stockfish
RUN ls -l stockfish/ && \
    mv stockfish/stockfish-ubuntu-x86-64-avx2 stockfish/stockfish && \
    chmod +x stockfish/stockfish && \
    file stockfish/stockfish

# Build application
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy JAR file
COPY --from=build /app/target/backend-chess-0.0.1-SNAPSHOT.jar app.jar

# Copy Stockfish binary to system path
COPY --from=build /app/stockfish/stockfish /usr/local/bin/stockfish

# Verify and set permissions
RUN chmod +x /usr/local/bin/stockfish

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
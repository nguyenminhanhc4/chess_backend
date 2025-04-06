# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy all files
COPY . .

# Verify and set permissions for Stockfish (Linux version)
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

# Verify in runtime stage
RUN ls -l /usr/local/bin/stockfish && \
    chmod +x /usr/local/bin/stockfish && \
    file /usr/local/bin/stockfish

# Install dependencies if needed
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    libgl1 \
    libxi6 \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
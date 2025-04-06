# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY . .

# Install dependencies và xử lý Stockfish
RUN apt-get update && \
    apt-get install -y file && \
    rm -rf /var/lib/apt/lists/* && \
    mv stockfish/stockfish-ubuntu-x86-64-avx2 /usr/local/bin/stockfish && \
    chmod +x /usr/local/bin/stockfish

# Build ứng dụng
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=build /app/target/backend-chess-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /usr/local/bin/stockfish /usr/local/bin/stockfish

# Cài đặt các phụ thuộc hệ thống
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    libgl1 \
    libxi6 \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
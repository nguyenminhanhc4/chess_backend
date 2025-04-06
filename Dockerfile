FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .

# Cấp quyền thực thi cho file Stockfish
RUN chmod +x src/main/resources/stockfish/stockfish

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy cả thư mục stockfish
COPY --from=build /app/target/backend-chess-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/src/main/resources/stockfish ./stockfish

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
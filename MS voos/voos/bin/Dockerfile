FROM maven:3.8.8-eclipse-temurin-17 AS builder

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 5001

# Add this line:
ENTRYPOINT ["java", "-jar", "app.jar"]
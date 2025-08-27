
# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/backend-1.0.0.jar app.jar
CMD ["java", "-jar", "app.jar"]

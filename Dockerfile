
# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:21-jdk
WORKDIR /app
# Copy the built JAR (use wildcard in case version changes)
COPY --from=build /app/target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]

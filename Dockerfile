# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21-jdk

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and pom.xml
COPY mvnw* pom.xml ./
COPY .mvn .mvn

# Copy the rest of the source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Copy the built jar to the container
COPY target/backend-1.0.0.jar app.jar

# Run the jar file
CMD ["java", "-jar", "app.jar"]

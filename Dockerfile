# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim

# Set the working directory in the container to /app
WORKDIR /app

# Add the current directory contents into the container at /app
ADD . /app

# Build your application with Maven or Gradle
RUN mvn clean package -DskipTests=true
# Or use Gradle: RUN ./gradlew build -x test

# Make port 4567 available to the world outside this container
EXPOSE 4567

# Run the application when the container launches
CMD ["java", "-jar", "target/java-api-1.0-SNAPSHOT-jar-with-dependencies.jar"]
# Or use Gradle: CMD ["java", "-jar", "build/libs/java-api-1.0-SNAPSHOT.jar"]
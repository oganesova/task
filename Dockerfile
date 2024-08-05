# Use the OpenJDK 19 image based on Alpine for a lightweight image
FROM openjdk:19-jdk-alpine

# Label the image with metadata
LABEL authors="Lilia"

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the target directory to the working directory in the container
COPY target/task-management-0.0.1-SNAPSHOT.jar app.jar

# Expose the port that the application will run on
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]

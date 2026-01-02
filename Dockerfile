FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the jar file
COPY target/kitly-mail-*.jar app.jar

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

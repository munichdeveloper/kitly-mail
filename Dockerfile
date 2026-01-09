# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl ca-certificates \
 && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/kitly-mail-*.jar app.jar

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-Dreactor.netty.native=false -Dio.netty.transport.noNative=true -Djava.net.preferIPv4Stack=true"
ENTRYPOINT ["java", "-jar", "app.jar"]
# ---------- Stage 1: build the jar with Maven ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy the Maven wrapper and pom first so dependencies are cached between builds
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline

COPY src src
RUN ./mvnw -q package -DskipTests

# ---------- Stage 2: small runtime image with only the JRE ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Run as a non-root user
RUN useradd --system --create-home appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

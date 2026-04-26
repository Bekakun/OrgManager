# ══════════════════════════════════════════════════════════════
# Stage 1 — Build the JAR with Maven
# ══════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy Maven wrapper first — changes here invalidate only the dep-download layer
COPY .mvn/  .mvn/
COPY mvnw   pom.xml ./

# Fix Windows CRLF line-endings so mvnw is executable inside Linux container
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Download all dependencies (cached as long as pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B --no-transfer-progress

# Copy source code and build
COPY src ./src
RUN ./mvnw package -DskipTests -B --no-transfer-progress

# ══════════════════════════════════════════════════════════════
# Stage 2 — Minimal runtime image
# ══════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create directory for uploaded document files
RUN mkdir -p /app/uploads

# Copy only the fat JAR from the build stage
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

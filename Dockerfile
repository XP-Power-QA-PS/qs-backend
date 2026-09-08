# ==========================================
# 1. Build Stage
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Cache dependencies and plugins
COPY pom.xml .
RUN mvn dependency:resolve-plugins dependency:resolve -B

# Copy source code and build package
COPY src ./src
RUN mvn package -DskipTests -B

# ==========================================
# 2. Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Run as non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy jar from builder
COPY --from=builder /app/target/*.jar app.jar

# Fix permissions
RUN chown -R appuser:appgroup /app
USER appuser

# Expose default port
EXPOSE 8080

# Environment variables default
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

# JVM optimizations for Cloud (512MB RAM) & On-Premise:
# 1. UseContainerSupport: tự động nhận diện giới hạn RAM của Docker
# 2. MaxRAMPercentage=75.0: giới hạn Heap tối đa 75% RAM (tránh bị Render OOM kill)
# 3. UseSerialGC: GC đơn luồng nhẹ nhất, tiết kiệm ~80MB RAM so với G1GC trên máy 1 core
# 4. ExitOnOutOfMemoryError: tự thoát để container tự khởi động lại nếu cạn RAM
# 5. java.security.egd: tăng tốc sinh chuỗi ngẫu nhiên cho JWT và SSL
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom -Dserver.port=${PORT} -jar app.jar"]

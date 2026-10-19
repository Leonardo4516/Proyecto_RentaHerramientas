# ==============================================================================
# Dockerfile Multietapa para RentaTools PRO (Spring Boot 4 / Java 21)
# ==============================================================================

# --- Etapa 1: Compilación y empaquetado del Fat JAR ---
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copiar archivos de Maven y POMs para cachear dependencias
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY domain/pom.xml domain/
COPY application/pom.xml application/
COPY infrastructure/pom.xml infrastructure/

# Descargar dependencias en caché
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true

# Copiar código fuente
COPY domain/src domain/src
COPY application/src application/src
COPY infrastructure/src infrastructure/src

# Compilar y empaquetar sin tests para build rápido
RUN ./mvnw clean package -DskipTests

# --- Etapa 2: Imagen de ejecución ligera (Alpine JRE) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario no root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar el JAR generado desde la etapa de compilación
COPY --from=builder /workspace/infrastructure/target/infrastructure-*.jar app.jar

# Asignar permisos al usuario
RUN chown -R appuser:appgroup /app
USER appuser

# Puerto por defecto de Spring Boot (Cloud asigna $PORT dinámicamente)
ENV PORT=8080
EXPOSE 8080

# Parámetros optimizados para contenedores de memoria limitada (Render / Railway / Cloud)
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT:-8080} -jar app.jar"]

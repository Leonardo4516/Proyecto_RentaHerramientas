#!/bin/bash
# Recompilar si no existe el war o si cambiaron archivos de configuración o código fuente
if [ ! -f "infrastructure/target/rentatools.war" ] || [ -n "$(find application domain infrastructure -type f -newer infrastructure/target/rentatools.war 2>/dev/null)" ] || [ "pom.xml" -nt "infrastructure/target/rentatools.war" ]; then
    echo "=========================================================="
    echo "📦 Compilando cambios de RentaTools..."
    echo "=========================================================="
    ./mvnw clean package -DskipTests
fi

# Detección automática de puerto libre para evitar colisiones
PORT=8080
if ss -tulpn 2>/dev/null | grep -q ":8080 " || lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1 || nc -z 127.0.0.1 8080 >/dev/null 2>&1; then
    echo "=========================================================="
    echo "⚠️  El puerto 8080 está ocupado por otro servicio (ej: Tomcat del sistema)."
    echo "🔄 Cambiando automáticamente al puerto 8081 para evitar errores..."
    echo "=========================================================="
    PORT=8081
fi

echo "=========================================================="
echo "🚀 Iniciando RentaTools en puerto $PORT (PostgreSQL Local / Docker)..."
echo "👉 Abre en tu navegador: http://localhost:$PORT/"
echo "👉 Swagger UI:          http://localhost:$PORT/swagger-ui.html"
echo "=========================================================="
java -Dserver.port=$PORT -jar infrastructure/target/rentatools.war

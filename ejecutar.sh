#!/bin/bash
if [ ! -f "infrastructure/target/rentatools.war" ]; then
    echo "=========================================================="
    echo "📦 Compilando RentaTools por primera vez..."
    echo "=========================================================="
    ./mvnw clean package -DskipTests
fi

# Detección automática de puerto para evitar conflictos
PORT=8080
if ss -tulpn 2>/dev/null | grep -q ":8080 " || lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1 || nc -z 127.0.0.1 8080 >/dev/null 2>&1; then
    echo "=========================================================="
    echo "⚠️  El puerto 8080 está ocupado por otro servicio (ej: Tomcat del sistema)."
    echo "🔄 Cambiando automáticamente al puerto 8081 para evitar errores..."
    echo "=========================================================="
    PORT=8081
fi

echo "=========================================================="
echo "🚀 Iniciando RentaTools en puerto $PORT (Tomcat Embebido + Supabase Cloud)..."
echo "👉 Abre en tu navegador: http://localhost:$PORT/"
echo "=========================================================="
java -Dserver.port=$PORT -jar infrastructure/target/rentatools.war

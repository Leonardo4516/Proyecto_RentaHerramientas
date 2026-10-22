#!/bin/bash
# ==============================================================================
# Script Inteligente de Ejecución - RentaTools (Linux / macOS)
# ==============================================================================

# 1. Recompilar automáticamente si el .war no existe o si se editaron archivos fuente
if [ ! -f "infrastructure/target/rentatools.war" ] || [ -n "$(find application domain infrastructure -type f -newer infrastructure/target/rentatools.war 2>/dev/null)" ] || [ "pom.xml" -nt "infrastructure/target/rentatools.war" ]; then
    echo "=========================================================="
    echo "📦 Compilando cambios de RentaTools..."
    echo "=========================================================="
    ./mvnw clean package -DskipTests
fi

# 2. Función para verificar si un puerto TCP está ocupado
is_port_in_use() {
    local p=$1
    if command -v ss >/dev/null 2>&1; then
        ss -tulpn 2>/dev/null | grep -q ":$p " && return 0
    fi
    if command -v lsof >/dev/null 2>&1; then
        lsof -Pi :$p -sTCP:LISTEN -t >/dev/null 2>&1 && return 0
    fi
    if command -v nc >/dev/null 2>&1; then
        nc -z 127.0.0.1 $p >/dev/null 2>&1 && return 0
    fi
    (echo > /dev/tcp/127.0.0.1/$p) >/dev/null 2>&1 && return 0
    return 1
}

# 3. Escaneo dinámico del primer puerto HTTP libre (iniciando en 8080)
PORT=8080
while is_port_in_use $PORT; do
    echo "⚠️  Puerto $PORT ocupado por otro proceso. Buscando siguiente puerto libre..."
    PORT=$((PORT + 1))
done

# 4. Verificación de Base de Datos (PostgreSQL / Docker / Fallback H2)
DB_OPTS=""
DB_INFO="PostgreSQL Local / Docker (localhost:5432)"

if [ "$1" == "--h2" ]; then
    echo "💾 Modo H2 en memoria solicitado explícitamente."
    DB_OPTS="-Dspring.datasource.url=jdbc:h2:mem:rentatools;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH -Dspring.datasource.driver-class-name=org.h2.Driver -Dspring.datasource.username=sa -Dspring.datasource.password="
    DB_INFO="H2 In-Memory (Autónomo)"
elif ! is_port_in_use 5432; then
    echo "⚠️  El puerto 5432 de PostgreSQL no está respondiendo."
    if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
        echo "🐳 Intentando iniciar PostgreSQL vía Docker Compose..."
        docker compose up -d postgres_db 2>/dev/null || docker-compose up -d postgres_db 2>/dev/null
        sleep 3
    fi
    # Si después de intentar con Docker sigue sin responder el puerto 5432:
    if ! is_port_in_use 5432; then
        echo "💡 No se detectó PostgreSQL ni Docker en el puerto 5432."
        echo "🛡️  Activando automáticamente modo de respaldo H2 (sin fallos ni dependencias externas)..."
        DB_OPTS="-Dspring.datasource.url=jdbc:h2:mem:rentatools;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH -Dspring.datasource.driver-class-name=org.h2.Driver -Dspring.datasource.username=sa -Dspring.datasource.password="
        DB_INFO="H2 In-Memory (Modo Respaldo Autónomo)"
    fi
fi

echo "=========================================================="
echo "🚀 Iniciando RentaTools en puerto $PORT"
echo "🗄️  Base de Datos:      $DB_INFO"
echo "👉 Abre en tu navegador: http://localhost:$PORT/"
echo "👉 Swagger UI:          http://localhost:$PORT/swagger-ui.html"
echo "=========================================================="

java -Dserver.port=$PORT $DB_OPTS -jar infrastructure/target/rentatools.war

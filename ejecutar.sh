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

# 4. Verificación de Base de Datos (Docker PostgreSQL puerto 5434 / Fallback)
DB_OPTS=""
DB_INFO="PostgreSQL Docker (localhost:5434)"

if [ "$1" == "--h2" ]; then
    echo "💾 Modo H2 en memoria solicitado explícitamente."
    DB_OPTS="-Dspring.datasource.url=jdbc:h2:mem:rentatools;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH -Dspring.datasource.driver-class-name=org.h2.Driver -Dspring.datasource.username=sa -Dspring.datasource.password="
    DB_INFO="H2 In-Memory (Autónomo)"
elif is_port_in_use 5434 && PGPASSWORD="postgres" psql -h localhost -p 5434 -U postgres -d renta_herramientas_db -c "SELECT 1;" >/dev/null 2>&1; then
    echo "🐳 Conectado a PostgreSQL en contenedor Docker (puerto 5434, base de datos 'renta_herramientas_db')."
    DB_OPTS="-Dspring.datasource.url=jdbc:postgresql://localhost:5434/renta_herramientas_db -Dspring.datasource.username=postgres -Dspring.datasource.password=postgres"
    DB_INFO="Docker PostgreSQL (rentatools_postgres en localhost:5434)"
elif is_port_in_use 5432 && PGPASSWORD="postgres" psql -h localhost -p 5432 -U postgres -d renta_herramientas_db -c "SELECT 1;" >/dev/null 2>&1; then
    echo "🐘 Conectado a PostgreSQL local (puerto 5432)."
    DB_OPTS="-Dspring.datasource.url=jdbc:postgresql://localhost:5432/renta_herramientas_db -Dspring.datasource.username=postgres -Dspring.datasource.password=postgres"
    DB_INFO="PostgreSQL Local (localhost:5432)"
else
    # Si Docker no está arriba, intentar levantarlo
    if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
        echo "🐳 Intentando iniciar PostgreSQL vía Docker Compose..."
        docker compose up -d postgres_db 2>/dev/null || docker-compose up -d postgres_db 2>/dev/null
        sleep 3
        if is_port_in_use 5434; then
            DB_OPTS="-Dspring.datasource.url=jdbc:postgresql://localhost:5434/renta_herramientas_db -Dspring.datasource.username=postgres -Dspring.datasource.password=postgres"
            DB_INFO="Docker PostgreSQL (rentatools_postgres en localhost:5434)"
        elif is_port_in_use 5432; then
            DB_OPTS="-Dspring.datasource.url=jdbc:postgresql://localhost:5432/renta_herramientas_db -Dspring.datasource.username=postgres -Dspring.datasource.password=postgres"
            DB_INFO="Docker PostgreSQL (rentatools_postgres en localhost:5432)"
        fi
    fi
    if [ -z "$DB_OPTS" ]; then
        echo "🛡️  No se detectó contenedor Docker en puerto 5434 ni 5432. Activando respaldo H2..."
        DB_OPTS="-Dspring.datasource.url=jdbc:h2:mem:rentatools;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH -Dspring.datasource.driver-class-name=org.h2.Driver -Dspring.datasource.username=sa -Dspring.datasource.password="
        DB_INFO="H2 In-Memory (Modo Respaldo Autónomo)"
    fi
fi

echo "=========================================================="
echo "🚀 Iniciando RentaTools en puerto $PORT"
echo "🗄️  Base de Datos:      $DB_INFO"
echo "👉 Abre en tu navegador: http://localhost:$PORT/"
echo "👉 Swagger UI:          http://localhost:$PORT/swagger-ui.html"
if [[ "$DB_INFO" == *"H2"* ]]; then
    echo "👉 Consola Web H2:      http://localhost:$PORT/h2-console"
    echo "   * JDBC URL:          jdbc:h2:mem:rentatools"
    echo "   * Usuario:           sa  |  Contraseña: (vacía)"
    echo "👉 Conexión DBeaver:    jdbc:h2:tcp://localhost:9092/mem:rentatools"
fi
echo "=========================================================="

java -Dserver.port=$PORT $DB_OPTS -jar infrastructure/target/rentatools.war

@echo off
setlocal enabledelayedexpansion
title RentaTools - Sistema de Renta de Maquinaria

REM 1. Compilar si no existe el artefacto .war
if not exist "infrastructure\target\rentatools.war" (
    echo ==========================================================
    echo Compilando RentaTools por primera vez...
    echo ==========================================================
    call mvnw.cmd clean package -DskipTests
)

REM 2. Escaneo dinámico del primer puerto HTTP libre (iniciando en 8080)
set PORT=8080
:check_port
netstat -ano | findstr /R ":%PORT% .*LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [AVISO] Puerto %PORT% ocupado por otro proceso. Probando siguiente...
    set /a PORT=%PORT%+1
    goto check_port
)

REM 3. Verificación de Base de Datos (PostgreSQL / Docker / Fallback H2)
set "DB_OPTS="
set "DB_INFO="

if "%1"=="--h2" (
    echo [INFO] Modo H2 en memoria solicitado explicitamente.
    set "DB_OPTS=-Dspring.datasource.url=jdbc:h2:mem:rentatools;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH -Dspring.datasource.driver-class-name=org.h2.Driver -Dspring.datasource.username=sa -Dspring.datasource.password="
    set "DB_INFO=H2 In-Memory (Autonomo)"
    goto start_app
)

REM Probar si el puerto 5434 de PostgreSQL Docker esta activo
netstat -ano | findstr /R ":5434 .*LISTENING" >nul 2>&1
if not errorlevel 1 (
    set "DB_OPTS=-Dspring.datasource.url=jdbc:postgresql://localhost:5434/renta_herramientas_db -Dspring.datasource.username=postgres -Dspring.datasource.password=postgres"
    set "DB_INFO=Docker PostgreSQL (localhost:5434)"
    where psql >nul 2>&1
    if not errorlevel 1 (
        set PGPASSWORD=postgres
        psql -h localhost -p 5434 -U postgres -d renta_herramientas_db -c "SELECT 1;" >nul 2>&1
        if errorlevel 1 (
            echo [AVISO] PostgreSQL en puerto 5434 rechazo la conexion o clave. Activando H2...
            set "DB_OPTS=-Dspring.datasource.url=jdbc:h2:mem:rentatools;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH -Dspring.datasource.driver-class-name=org.h2.Driver -Dspring.datasource.username=sa -Dspring.datasource.password="
            set "DB_INFO=H2 In-Memory (Modo Respaldo Autonomo)"
        )
    )
    goto start_app
)

REM Probar si el puerto 5432 de PostgreSQL Local esta activo
netstat -ano | findstr /R ":5432 .*LISTENING" >nul 2>&1
if not errorlevel 1 (
    set "DB_OPTS=-Dspring.datasource.url=jdbc:postgresql://localhost:5432/renta_herramientas_db -Dspring.datasource.username=postgres -Dspring.datasource.password=postgres"
    set "DB_INFO=PostgreSQL Local (localhost:5432)"
    where psql >nul 2>&1
    if not errorlevel 1 (
        set PGPASSWORD=postgres
        psql -h localhost -p 5432 -U postgres -d renta_herramientas_db -c "SELECT 1;" >nul 2>&1
        if errorlevel 1 (
            echo [AVISO] PostgreSQL en puerto 5432 rechazo la conexion o clave. Activando H2...
            set "DB_OPTS=-Dspring.datasource.url=jdbc:h2:mem:rentatools;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH -Dspring.datasource.driver-class-name=org.h2.Driver -Dspring.datasource.username=sa -Dspring.datasource.password="
            set "DB_INFO=H2 In-Memory (Modo Respaldo Autonomo)"
        )
    )
    goto start_app
)

REM Si ninguno esta activo, intentar iniciar Docker Compose
echo [AVISO] Ni puerto 5434 ni 5432 estan respondiendo.
docker info >nul 2>&1
if not errorlevel 1 (
    echo [DOCKER] Intentando iniciar PostgreSQL via Docker Compose...
    docker compose up -d postgres_db >nul 2>&1 || docker-compose up -d postgres_db >nul 2>&1
    timeout /t 3 /nobreak >nul 2>&1
    netstat -ano | findstr /R ":5434 .*LISTENING" >nul 2>&1
    if not errorlevel 1 (
        set "DB_OPTS=-Dspring.datasource.url=jdbc:postgresql://localhost:5434/renta_herramientas_db -Dspring.datasource.username=postgres -Dspring.datasource.password=postgres"
        set "DB_INFO=Docker PostgreSQL (localhost:5434)"
        goto start_app
    )
    netstat -ano | findstr /R ":5432 .*LISTENING" >nul 2>&1
    if not errorlevel 1 (
        set "DB_OPTS=-Dspring.datasource.url=jdbc:postgresql://localhost:5432/renta_herramientas_db -Dspring.datasource.username=postgres -Dspring.datasource.password=postgres"
        set "DB_INFO=Docker PostgreSQL (localhost:5432)"
        goto start_app
    )
)

echo [RESPALDO] No se detecto PostgreSQL ni Docker disponible.
echo [RESPALDO] Activando modo de respaldo autonomo H2 (sin dependencias externas)...
set "DB_OPTS=-Dspring.datasource.url=jdbc:h2:mem:rentatools;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH -Dspring.datasource.driver-class-name=org.h2.Driver -Dspring.datasource.username=sa -Dspring.datasource.password="
set "DB_INFO=H2 In-Memory (Modo Respaldo Autonomo)"

:start_app
echo ==========================================================
echo Iniciando RentaTools en puerto %PORT%
echo Base de Datos:      %DB_INFO%
echo Abre en tu navegador: http://localhost:%PORT%/
echo Swagger UI:          http://localhost:%PORT%/swagger-ui.html
echo %DB_INFO% | findstr /I "H2" >nul 2>&1
if not errorlevel 1 (
    echo Consola Web H2:      http://localhost:%PORT%/h2-console
    echo   * JDBC URL:        jdbc:h2:mem:rentatools
    echo   * Usuario:         sa  ^|  Clave: (vacia)
    echo Conexion DBeaver:    jdbc:h2:tcp://localhost:9092/mem:rentatools
)
echo ==========================================================

java -Dserver.port=%PORT% %DB_OPTS% -jar infrastructure\target\rentatools.war

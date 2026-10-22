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
set "DB_INFO=PostgreSQL Local / Docker (localhost:5432)"

if "%1"=="--h2" (
    echo [INFO] Modo H2 en memoria solicitado explicitamente.
    set "DB_OPTS=-Dspring.datasource.url=jdbc:h2:mem:rentatools;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH -Dspring.datasource.driver-class-name=org.h2.Driver -Dspring.datasource.username=sa -Dspring.datasource.password="
    set "DB_INFO=H2 In-Memory (Autonomo)"
    goto start_app
)

netstat -ano | findstr /R ":5432 .*LISTENING" >nul 2>&1
if errorlevel 1 (
    echo [AVISO] El puerto 5432 de PostgreSQL no esta respondiendo.
    docker info >nul 2>&1
    if not errorlevel 1 (
        echo [DOCKER] Intentando iniciar PostgreSQL via Docker Compose...
        docker compose up -d postgres_db >nul 2>&1 || docker-compose up -d postgres_db >nul 2>&1
        timeout /t 3 /nobreak >nul 2>&1
    )
    netstat -ano | findstr /R ":5432 .*LISTENING" >nul 2>&1
    if errorlevel 1 (
        echo [RESPALDO] No se detecto PostgreSQL ni Docker en puerto 5432.
        echo [RESPALDO] Activando modo de respaldo autonomo H2 (sin fallos ni dependencias)...
        set "DB_OPTS=-Dspring.datasource.url=jdbc:h2:mem:rentatools;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH -Dspring.datasource.driver-class-name=org.h2.Driver -Dspring.datasource.username=sa -Dspring.datasource.password="
        set "DB_INFO=H2 In-Memory (Modo Respaldo Autonomo)"
    )
)

:start_app
echo ==========================================================
echo Iniciando RentaTools en puerto %PORT%
echo Base de Datos:      %DB_INFO%
echo Abre en tu navegador: http://localhost:%PORT%/
echo Swagger UI:          http://localhost:%PORT%/swagger-ui.html
echo ==========================================================

java -Dserver.port=%PORT% %DB_OPTS% -jar infrastructure\target\rentatools.war

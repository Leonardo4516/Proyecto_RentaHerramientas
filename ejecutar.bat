@echo off
if not exist "infrastructure\target\rentatools.war" (
    echo ==========================================================
    echo Compilando RentaTools por primera vez...
    echo ==========================================================
    call mvnw.cmd clean package -DskipTests
)

set PORT=8080
netstat -ano | findstr :8080 | findstr LISTENING >nul 2>&1
if not errorlevel 1 (
    echo ==========================================================
    echo Puerto 8080 ocupado. Cambiando automaticamente a puerto 8081...
    echo ==========================================================
    set PORT=8081
)

echo ==========================================================
echo Iniciando RentaTools en puerto %PORT% (Tomcat Embebido + Supabase Cloud)...
echo Abre en tu navegador: http://localhost:%PORT%/
echo ==========================================================
java -Dserver.port=%PORT% -jar infrastructure\target\rentatools.war

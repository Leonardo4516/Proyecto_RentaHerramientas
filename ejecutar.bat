@echo off
if not exist "infrastructure\target\rentatools.war" (
    echo ==========================================================
    echo Compilando RentaTools por primera vez...
    echo ==========================================================
    call mvnw.cmd clean package -DskipTests
)

echo ==========================================================
echo Iniciando RentaTools (Tomcat Embebido + Supabase Cloud)...
echo ==========================================================
java -jar infrastructure\target\rentatools.war

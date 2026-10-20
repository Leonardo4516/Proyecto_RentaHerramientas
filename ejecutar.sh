#!/bin/bash
if [ ! -f "infrastructure/target/rentatools.war" ]; then
    echo "=========================================================="
    echo "📦 Compilando RentaTools por primera vez..."
    echo "=========================================================="
    ./mvnw clean package -DskipTests
fi

echo "=========================================================="
echo "🚀 Iniciando RentaTools (Tomcat Embebido + Supabase Cloud)..."
echo "=========================================================="
java -jar infrastructure/target/rentatools.war

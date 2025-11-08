#!/bin/bash
echo "===== Building Transfers API ====="

# Dar permisos a gradlew
chmod +x gradlew

# Limpiar y compilar
./gradlew clean build -x test

echo "===== Build Complete ====="

chmod +x build.sh
# ============================================
# ETAPA 1: Build (Compilar la aplicación)
# ============================================
FROM gradle:8.5-jdk17-alpine AS builder

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Gradle
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Copiar código fuente
COPY src ./src

# AGREGAR ESTA LÍNEA: Dar permisos de ejecución a gradlew
RUN chmod +x gradlew

# Compilar la aplicación (sin tests para acelerar)
RUN ./gradlew clean build -x test --no-daemon

# ============================================
# ETAPA 2: Runtime (Ejecutar la aplicación)
# ============================================
FROM eclipse-temurin:17-jre-alpine

# Instalar curl para health checks
RUN apk add --no-cache curl

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Establecer directorio de trabajo
WORKDIR /app

# Copiar JAR desde la etapa de build
COPY --from=builder /app/build/libs/*.jar app.jar

# Exponer puerto (Render asigna dinámicamente)
EXPOSE ${PORT:-8080}

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -XX:MaxMetaspaceSize=256m"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/api/accounts || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Dserver.port=${PORT} -jar app.jar"]
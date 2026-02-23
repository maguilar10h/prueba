# 🐳 Dockerización de la Aplicación

Guía completa para ejecutar la aplicación usando Docker y Docker Compose.

## 📋 Tabla de Contenidos

- [Requisitos Previos](#requisitos-previos)
- [Inicio Rápido](#inicio-rápido)
- [Arquitectura Docker](#arquitectura-docker)
- [Servicios](#servicios)
- [Configuración](#configuración)
- [Comandos Útiles](#comandos-útiles)
- [Migraciones de Base de Datos](#migraciones-de-base-de-datos)
- [Desarrollo](#desarrollo)
- [Producción](#producción)
- [Troubleshooting](#troubleshooting)

## 🔧 Requisitos Previos

- **Docker Desktop** o **Docker Engine** 20.10+
- **Docker Compose** 2.0+
- Mínimo **4GB RAM** disponible
- **Java 21** (solo para desarrollo local, no requerido para Docker)

### Verificar Instalación

```bash
# Verificar Docker
docker --version
docker-compose --version

# Verificar que Docker está corriendo
docker ps
```

## 🚀 Inicio Rápido

### Opción 1: Ejecución Completa (Recomendado)

```bash
# Construir y levantar todos los servicios
docker-compose up --build

# Ejecutar en segundo plano (detached mode)
docker-compose up -d --build

# Ver logs de la aplicación
docker-compose logs -f app

# Ver logs de todos los servicios
docker-compose logs -f

# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes (⚠️ elimina datos)
docker-compose down -v
```

### Opción 2: Build Manual de la Imagen

```bash
# Construir la imagen
docker build -t prueba-app:latest .

# Ejecutar el contenedor (requiere servicios externos)
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_HOST=host.docker.internal \
  -e SPRING_DATA_REDIS_HOST=host.docker.internal \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  prueba-app:latest
```

### Verificar que Todo Funciona

```bash
# Health check de la aplicación
curl http://localhost:8080/actuator/health

# Swagger UI
open http://localhost:8080/swagger-ui.html

# API Docs
open http://localhost:8080/v3/api-docs
```

## 🏗️ Arquitectura Docker

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Network                        │
│                  (prueba-network)                       │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │   App        │  │  PostgreSQL  │  │    Redis     │ │
│  │  :8080       │  │   :5432      │  │   :6379      │ │
│  └──────┬───────┘  └──────────────┘  └──────────────┘ │
│         │                                               │
│  ┌──────▼───────┐  ┌──────────────┐                   │
│  │   Kafka      │  │  Zookeeper   │                   │
│  │  :9092       │  │   :2181      │                   │
│  └──────────────┘  └──────────────┘                   │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### Archivos Docker

- **`Dockerfile`**: Imagen multi-stage para construir y ejecutar la aplicación
- **`docker-compose.yml`**: Orquestación de todos los servicios
- **`.dockerignore`**: Archivos excluidos del build
- **`application-docker.properties`**: Configuración específica para entorno Docker

## 🛠️ Servicios

### 1. Aplicación Spring Boot (`app`)

- **Puerto**: `8080` (host) → `8080` (contenedor)
- **Health Check**: `http://localhost:8080/actuator/health`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Imagen**: Construida desde `Dockerfile` (multi-stage build)
- **Dependencias**: PostgreSQL, Redis, Kafka

**Características:**
- Build multi-stage optimizado
- Usuario no-root para seguridad
- Health checks configurados
- Logs estructurados

### 2. PostgreSQL (`postgres`)

- **Puerto**: `5432` (host) → `5432` (contenedor)
- **Base de datos**: `prueba_db`
- **Usuario**: `prueba_user`
- **Contraseña**: `prueba_password`
- **Volumen**: `postgres-data` (persistencia de datos)
- **Imagen**: `postgres:16-alpine`

**Conexión desde host:**
```bash
psql -h localhost -U prueba_user -d prueba_db
# Password: prueba_password
```

### 3. Redis (`redis`)

- **Puerto**: `6380` (host) → `6379` (contenedor)
- **Volumen**: `redis-data` (persistencia AOF)
- **Imagen**: `redis:7-alpine`
- **Configuración**: AOF (Append Only File) habilitado

**Nota**: El puerto del host es `6380` para evitar conflictos con Redis local. Para desarrollo local, ajusta la configuración si es necesario.

**Conexión desde host:**
```bash
redis-cli -h localhost -p 6380
```

### 4. Kafka (`kafka`)

- **Puerto interno**: `9092` (dentro de Docker network)
- **Puerto host**: `9093` (para conexión desde host)
- **Imagen**: `confluentinc/cp-kafka:7.5.0`
- **Dependencia**: Zookeeper

**Topics creados automáticamente:**
- `order-events`: Eventos de órdenes
- `inventory-events`: Eventos de inventario
- `notifications`: Notificaciones

**Conexión desde host:**
```bash
# Usar puerto 9093 para conexión desde el host
export KAFKA_BOOTSTRAP_SERVERS=localhost:9093
```

### 5. Zookeeper (`zookeeper`)

- **Puerto**: `2181` (interno, no expuesto)
- **Imagen**: `confluentinc/cp-zookeeper:7.5.0`
- **Propósito**: Coordinación de Kafka

## ⚙️ Configuración

### Variables de Entorno

Puedes configurar las siguientes variables en `docker-compose.yml` o mediante un archivo `.env`:

```env
# JWT Configuration
JWT_SECRET=your-secret-key-change-in-production-use-strong-random-key-minimum-256-bits

# Database Configuration
SPRING_DATASOURCE_HOST=postgres
SPRING_DATASOURCE_DB=prueba_db
SPRING_DATASOURCE_USERNAME=prueba_user
SPRING_DATASOURCE_PASSWORD=prueba_password

# Redis Configuration
SPRING_DATA_REDIS_HOST=redis

# Kafka Configuration
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Spring Profile
SPRING_PROFILES_ACTIVE=docker
```

### Crear Archivo `.env`

```bash
# Copiar y editar variables de entorno
cp .env.example .env
# Editar .env con tus valores
```

### Health Checks

Todos los servicios tienen health checks configurados:

```bash
# Ver estado de health checks
docker-compose ps

# Verificar health check de la app
curl http://localhost:8080/actuator/health

# Verificar health check de PostgreSQL
docker-compose exec postgres pg_isready -U prueba_user

# Verificar health check de Redis
docker-compose exec redis redis-cli ping
```

## 📝 Comandos Útiles

### Gestión de Servicios

```bash
# Ver estado de todos los servicios
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f app
docker-compose logs -f postgres
docker-compose logs -f redis
docker-compose logs -f kafka

# Reiniciar un servicio específico
docker-compose restart app
docker-compose restart postgres

# Detener un servicio específico
docker-compose stop app

# Iniciar un servicio específico
docker-compose start app

# Reconstruir un servicio específico
docker-compose up -d --build app
```

### Ejecución de Comandos

```bash
# Ejecutar shell dentro del contenedor de la app
docker-compose exec app sh

# Ejecutar comandos en PostgreSQL
docker-compose exec postgres psql -U prueba_user -d prueba_db

# Ejecutar comandos en Redis
docker-compose exec redis redis-cli

# Ver variables de entorno del contenedor
docker-compose exec app env
```

### Limpieza

```bash
# Detener y eliminar contenedores
docker-compose down

# Detener y eliminar contenedores + volúmenes (⚠️ elimina datos)
docker-compose down -v

# Detener y eliminar contenedores + volúmenes + imágenes
docker-compose down -v --rmi all

# Limpiar sistema Docker (⚠️ elimina todo)
docker system prune -a --volumes
```

### Inspección

```bash
# Ver uso de recursos
docker stats

# Ver información de un contenedor
docker inspect prueba-app

# Ver logs de un contenedor específico
docker logs -f prueba-app

# Ver procesos dentro de un contenedor
docker-compose exec app ps aux
```

## 🗄️ Migraciones de Base de Datos

### Migraciones Automáticas

Las migraciones de Flyway se ejecutan **automáticamente** al iniciar la aplicación. No necesitas ejecutarlas manualmente.

### Scripts de Migración

El proyecto incluye scripts para ejecutar migraciones manualmente:

**Linux/macOS:**
```bash
# Migraciones para base de datos local
./migrate.sh local

# Migraciones para base de datos en Docker
./migrate.sh docker
```

**Windows:**
```bash
# Migraciones para base de datos local
migrate.bat local

# Migraciones para base de datos en Docker
migrate.bat docker
```

### Ejecutar Migraciones con Gradle

```bash
# Ejecutar migraciones (base de datos local)
./gradlew flywayMigrate

# Ejecutar migraciones (especificar URL)
./gradlew flywayMigrate \
  -Pflyway.url=jdbc:postgresql://localhost:5432/prueba_db \
  -Pflyway.user=prueba_user \
  -Pflyway.password=prueba_password

# Ver estado de las migraciones
./gradlew flywayInfo

# Validar migraciones
./gradlew flywayValidate

# Ver historial de migraciones
./gradlew flywayHistory

# Reparar migraciones (si hay problemas)
./gradlew flywayRepair

# Limpiar base de datos (⚠️ CUIDADO! Elimina todas las tablas)
./gradlew flywayClean
```

### Verificar Migraciones

```bash
# Conectarse a PostgreSQL
docker-compose exec postgres psql -U prueba_user -d prueba_db

# Dentro de psql:
\dt                          # Listar todas las tablas
SELECT * FROM flyway_schema_history;  # Ver historial de migraciones
\q                           # Salir
```

## 💻 Desarrollo

### Desarrollo Local sin Docker

```bash
# Ejecutar aplicación localmente
./gradlew bootRun

# Ejecutar con perfil específico
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Desarrollo con Docker (Servicios Externos)

```bash
# Levantar solo servicios de infraestructura
docker-compose up -d postgres redis kafka zookeeper

# Ejecutar app localmente conectada a servicios Docker
./gradlew bootRun --args='--spring.profiles.active=docker'
```

### Hot Reload en Desarrollo

Para desarrollo con hot reload, puedes montar el código fuente:

```yaml
# Agregar a docker-compose.yml (solo desarrollo)
volumes:
  - ./src:/app/src
```

### Debugging

```bash
# Ejecutar con debug habilitado
docker-compose run --service-ports -e JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005" app

# Conectar debugger en puerto 5005
```

## 🚢 Producción

### Consideraciones para Producción

1. **Seguridad**
   - ✅ Cambiar todas las contraseñas por defecto
   - ✅ Usar Docker secrets o variables de entorno seguras
   - ✅ Configurar JWT secret fuerte
   - ✅ Usar HTTPS/TLS
   - ✅ Configurar firewall y network policies

2. **Base de Datos**
   - ✅ Usar PostgreSQL gestionado (RDS, Cloud SQL, etc.)
   - ✅ Configurar backups automáticos
   - ✅ Configurar replicación para alta disponibilidad
   - ✅ Ajustar connection pool según carga

3. **Recursos**
   - ✅ Configurar límites de CPU/memoria
   - ✅ Configurar resource limits
   - ✅ Monitorear uso de recursos

4. **Logging y Monitoreo**
   - ✅ Configurar logging centralizado (ELK, Loki, etc.)
   - ✅ Integrar Prometheus/Grafana
   - ✅ Configurar alertas
   - ✅ Health checks y readiness probes

5. **Escalabilidad**
   - ✅ Configurar múltiples réplicas de la app
   - ✅ Usar load balancer
   - ✅ Configurar Kafka con múltiples brokers
   - ✅ Redis cluster para alta disponibilidad

### Ejemplo de docker-compose.prod.yml

```yaml
version: '3.8'

services:
  app:
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '1'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
    restart: always
    # ... otras configuraciones
```

## 🔍 Troubleshooting

### La aplicación no inicia

**Síntoma**: El contenedor se reinicia constantemente

**Solución**:
```bash
# Ver logs detallados
docker-compose logs app

# Verificar que los servicios dependientes estén listos
docker-compose ps

# Verificar health checks
curl http://localhost:8080/actuator/health
```

### No puede conectar a PostgreSQL

**Síntoma**: Errores de conexión a base de datos

**Solución**:
```bash
# Verificar que PostgreSQL esté corriendo
docker-compose ps postgres

# Ver logs de PostgreSQL
docker-compose logs postgres

# Verificar que el health check haya pasado
docker-compose exec postgres pg_isready -U prueba_user

# Conectarse manualmente para probar
docker-compose exec postgres psql -U prueba_user -d prueba_db
```

### No puede conectar a Redis

**Síntoma**: Errores de conexión a Redis

**Solución**:
```bash
# Verificar que Redis esté corriendo
docker-compose ps redis

# Probar conexión
docker-compose exec redis redis-cli ping

# Ver logs
docker-compose logs redis
```

### No puede conectar a Kafka

**Síntoma**: Errores de conexión a Kafka

**Solución**:
```bash
# Verificar que Kafka y Zookeeper estén corriendo
docker-compose ps kafka zookeeper

# Ver logs de Kafka (puede tardar en iniciar)
docker-compose logs kafka

# Verificar que Kafka esté listo (puede tardar 60+ segundos)
docker-compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Puerto ya está en uso

**Síntoma**: Error "port is already allocated"

**Solución**:
```bash
# Ver qué está usando el puerto
lsof -i :8080    # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Cambiar puerto en docker-compose.yml
ports:
  - "8081:8080"  # Usar puerto diferente
```

### Las migraciones no se ejecutan

**Síntoma**: Tablas no existen después de iniciar

**Solución**:
```bash
# Ver logs de Flyway
docker-compose logs app | grep -i flyway

# Reiniciar con volúmenes limpios
docker-compose down -v
docker-compose up --build

# Ejecutar migraciones manualmente
./gradlew flywayMigrate -Pflyway.url=jdbc:postgresql://localhost:5432/prueba_db \
  -Pflyway.user=prueba_user -Pflyway.password=prueba_password
```

### Problemas de Memoria

**Síntoma**: Contenedores se detienen por falta de memoria

**Solución**:
```bash
# Ver uso de recursos
docker stats

# Aumentar memoria disponible en Docker Desktop
# Settings → Resources → Memory

# Reducir servicios innecesarios
docker-compose up -d postgres redis kafka zookeeper app
```

### Limpiar Todo y Empezar de Nuevo

```bash
# ⚠️ ADVERTENCIA: Esto elimina TODOS los datos

# Detener y eliminar todo
docker-compose down -v --rmi all

# Limpiar sistema Docker
docker system prune -a --volumes

# Reconstruir desde cero
docker-compose up --build
```

## 📚 Recursos Adicionales

- [Documentación de Docker](https://docs.docker.com/)
- [Documentación de Docker Compose](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Flyway Documentation](https://flywaydb.org/documentation/)

## 🆘 Soporte

Si encuentras problemas:

1. Revisa los logs: `docker-compose logs -f`
2. Verifica los health checks: `docker-compose ps`
3. Consulta la sección [Troubleshooting](#troubleshooting)
4. Revisa la documentación de arquitectura en `docs/architecture.md`

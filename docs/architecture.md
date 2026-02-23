# Arquitectura del Sistema

## Clean Architecture

Este sistema está diseñado siguiendo los principios de **Clean Architecture**.

## Capas de la Arquitectura

### 1. Domain Layer (Capa más interna - Enterprise Business Rules)

**Ubicación**: `com.informacolombia.prueba.domain`

Esta es la capa más interna y contiene las reglas de negocio puras. No depende de ninguna otra capa.

**Componentes**:
- **Entities**: Entidades de dominio que encapsulan lógica de negocio
  - `Order`, `OrderItem`, `Product`, `User`, `Inventory`, `Role`
- **Value Objects**: Objetos inmutables que representan conceptos del dominio
  - `OrderId`, `ProductId`, `UserId`, `Money`, `Quantity`, `OrderStatus`
- **Domain Events**: Eventos que representan algo importante que ocurrió en el dominio
  - `OrderCreatedEvent`, `OrderStatusChangedEvent`, `InventoryReservedEvent`, `InventoryReleasedEvent`
- **Domain Services**: Servicios que contienen lógica de dominio que no pertenece a una entidad específica
  - `OrderStateMachine` - Máquina de estados para el ciclo de vida del pedido
- **Repository Interfaces**: Interfaces que definen contratos para persistencia (puertos de salida)
  - `OrderRepository`, `InventoryRepository`, `UserRepository`, `ProductRepository`

**Principios**:
- No depende de frameworks externos
- No depende de la capa de aplicación
- Contiene solo lógica de negocio pura

### 2. Application Layer (Application Business Rules)

**Ubicación**: `com.informacolombia.prueba.application`

Contiene la lógica de aplicación y casos de uso. Orquesta las entidades de dominio para cumplir con los casos de uso.

**Componentes**:
- **Use Cases**: Casos de uso que implementan la lógica de aplicación
  - `CreateOrderUseCase`, `UpdateOrderUseCase`, `CancelOrderUseCase`, `GetOrderUseCase`
  - `ReserveInventoryUseCase`, `ReleaseInventoryUseCase`, `CheckAvailabilityUseCase`
  - `LoginUseCase`, `RegisterUseCase`, `GetCurrentUserUseCase`
- **Ports (Inbound)**: Interfaces que definen cómo los adaptadores de entrada pueden interactuar con la aplicación
  - `OrderInputPort` - Define métodos que los controllers pueden usar
  - `OrderInputPortImpl` - Implementación del puerto de entrada
- **Ports (Outbound)**: Interfaces que definen cómo la aplicación interactúa con servicios externos
  - `EventPublisher` - Para publicar eventos de dominio
  - `CacheRepository` - Para operaciones de caché
  - `TokenProvider` - Para generación de tokens JWT
- **Saga**: Patrones Saga para transacciones distribuidas
  - `OrderSaga` - Orquestación de transacciones distribuidas

**Principios**:
- Depende solo del dominio
- No conoce detalles de implementación de infraestructura
- Define contratos (puertos) que los adaptadores implementan

### 3. Presentation Layer (Interface Adapters - Inbound)

**Ubicación**: `com.informacolombia.prueba.presentation`

Esta capa contiene adaptadores de entrada que reciben datos del mundo exterior y los convierten para la capa de aplicación.

**Componentes**:
- **REST Controllers**: Controladores REST que manejan peticiones HTTP
  - `OrderController` - Endpoints para gestión de pedidos
  - `AuthController` - Endpoints para autenticación y registro
- **DTOs**: Data Transfer Objects que representan datos de entrada/salida
  - `OrderDTO`, `OrderItemDTO`, `OrderResponse`
  - `CreateOrderRequest`
  - `LoginResponse`, `RegisterResponse`, `UserInfo`
- **Mappers**: Mappers que convierten entre DTOs y entidades de dominio
  - Directorio `mapper/` (preparado para MapStruct si se requiere)
- **Exception Handlers**: Manejo centralizado de excepciones
  - `GlobalExceptionHandler` - Manejo global de excepciones HTTP

### 4. Infrastructure Layer (Interface Adapters - Outbound)

**Ubicación**: `com.informacolombia.prueba.infrastructure`

Esta capa contiene adaptadores de salida que implementan los puertos definidos en la capa de aplicación y configuraciones de frameworks.

**Componentes**:
- **Persistence**: Implementaciones JPA de los repositorios
  - `JpaOrderRepository`, `JpaInventoryRepository`, `JpaUserRepository`, `JpaProductRepository`
- **Event**: Implementaciones de publicación y consumo de eventos
  - `KafkaEventPublisher` - Implementa `EventPublisher` para publicar eventos en Kafka
  - `KafkaEventConsumer` - Consume eventos de Kafka
- **Cache**: Implementaciones de caché
  - `RedisInventoryCache` - Implementa `CacheRepository` para caché de inventario en Redis
  - `JacksonJsonRedisSerializer` - Serializador JSON para Redis
- **Security**: Implementaciones de seguridad
  - `JwtTokenProvider` - Implementa `TokenProvider` para generación y validación de tokens JWT
  - `JwtAuthenticationFilter` - Filtro de autenticación JWT para Spring Security
- **Config**: Configuraciones de frameworks y drivers externos
  - `SecurityConfig` - Configuración de Spring Security
  - `KafkaConfig` - Configuración de Kafka
  - `JpaConfig` - Configuración de JPA
  - `RedisConfig` - Configuración de Redis
  - `RetryConfig` - Configuración de reintentos
  - `SwaggerConfig` - Configuración de Swagger/OpenAPI
  - `ApplicationConfig` - Configuración general de la aplicación
  - `DataInitializer` - Inicialización de datos

## Diagrama de Dependencias

```
┌─────────────────────────────────────────────────────────┐
│     Presentation Layer                                   │
│  - REST Controllers (OrderController, AuthController)    │
│  - DTOs, Exception Handlers                              │
└─────────────────┬───────────────────────────────────────┘
                  │ depende de
┌─────────────────▼───────────────────────────────────────┐
│     Infrastructure Layer                                 │
│  - JPA Repositories (outbound)                           │
│  - Kafka Adapters (outbound)                             │
│  - Redis Cache (outbound)                                │
│  - JWT Security (outbound)                               │
│  - Config (Spring Security, Kafka, Redis, JPA)           │
└─────────────────┬───────────────────────────────────────┘
                  │ implementa
┌─────────────────▼───────────────────────────────────────┐
│     Application Layer (Use Cases)                        │
│  - CreateOrderUseCase, UpdateOrderUseCase, etc.          │
│  - Ports (interfaces)                                    │
│  - OrderSaga                                             │
└─────────────────┬───────────────────────────────────────┘
                  │ depende de
┌─────────────────▼───────────────────────────────────────┐
│     Domain Layer (Entities, Value Objects)               │
│  - Order, Product, User, Inventory                       │
│  - Repository Interfaces                                 │
│  - Domain Events, Domain Services                        │
└─────────────────────────────────────────────────────────┘
```

## Principios de Diseño Aplicados

### SOLID Principles

1. **Single Responsibility**: Cada clase tiene una única razón para cambiar
2. **Open/Closed**: Abierto para extensión, cerrado para modificación
3. **Liskov Substitution**: Las implementaciones pueden sustituirse sin romper el código
4. **Interface Segregation**: Interfaces específicas en lugar de interfaces generales
5. **Dependency Inversion**: Depender de abstracciones, no de implementaciones concretas

### Domain-Driven Design (DDD)

- **Entities**: Objetos con identidad única
- **Value Objects**: Objetos inmutables definidos por sus valores
- **Domain Events**: Eventos que representan cambios importantes en el dominio
- **Aggregates**: Agregados que mantienen la consistencia del dominio
- **Repositories**: Abstracciones para persistencia

### Clean Architecture Benefits

1. **Independencia de Frameworks**: El dominio no depende de Spring, JPA, etc.
2. **Testabilidad**: Fácil de testear sin mocks complejos
3. **Independencia de UI**: Puede cambiar de REST a GraphQL sin afectar el dominio
4. **Independencia de Base de Datos**: Puede cambiar de H2 a PostgreSQL sin afectar el dominio
5. **Independencia de Agentes Externos**: El negocio no depende de detalles externos

## Flujo de Datos

### Crear un Pedido (Ejemplo)

1. **Controller** (`OrderController` en `presentation.rest`) recibe `POST /api/v1/orders` con `CreateOrderRequest`
2. El controller invoca el **Use Case** (`CreateOrderUseCase`) directamente o a través de `OrderInputPort`
3. **Use Case** (`CreateOrderUseCase`) ejecuta la lógica de negocio:
   - Valida productos usando `ProductRepository` (interfaz del dominio)
   - Valida disponibilidad de inventario
   - Crea la entidad `Order` con `OrderItems`
   - Guarda usando `OrderRepository` (interfaz del dominio)
   - Publica eventos de dominio usando `EventPublisher` (interfaz)
4. **Infrastructure** (`JpaOrderRepository` en `infrastructure.persistence`) implementa `OrderRepository` y persiste en PostgreSQL
5. **Infrastructure** (`KafkaEventPublisher` en `infrastructure.event`) implementa `EventPublisher` y publica eventos en Kafka
6. El controller convierte la entidad `Order` → `OrderDTO` o `OrderResponse`
7. **Controller** retorna el DTO como respuesta HTTP

## Manejo de Concurrencia

- **Optimistic Locking**: Uso de `@Version` en entidades JPA
- **Retry Mechanism**: Reintentos automáticos en caso de conflictos
- **Event Sourcing**: Auditoría de cambios mediante eventos
- **Cache Distribuida**: Redis para información frecuentemente accedida

## Testing Strategy

- **Domain Tests**: Tests unitarios puros del dominio (sin frameworks)
- **Use Case Tests**: Tests de casos de uso con mocks de repositorios
- **Integration Tests**: Tests de integración con Testcontainers
- **Concurrency Tests**: Tests específicos para validar concurrencia
- **Load Tests**: Tests de carga con JMeter

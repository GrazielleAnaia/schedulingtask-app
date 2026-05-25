# Scheduling Tasks App - Microservices

A task scheduling application built with Spring Boot microservices. The system allows customers to register, manage their profiles, create scheduled tasks, and receive task notifications through asynchronous messaging.

This project demonstrates service discovery, centralized configuration, API gateway routing, OAuth2/JWT security, rate limiting, circuit breakers, asynchronous event processing, and independent service ownership.

## Architecture

| Service | Description |
| --- | --- |
| `gateway` | API Gateway responsible for routing, OAuth2/JWT validation, rate limiting, circuit breakers, fallback handling, and forwarding authenticated user context |
| `eureka` | Service discovery server used by services to register themselves and discover other services |
| `configserver` | Centralized configuration server that serves profile-specific YAML files |
| `registration-api` | Manages customers, profiles, addresses, phones, account status, and admin customer operations |
| `scheduling-api` | Manages task creation, updates, filtering, deletion, and notification status |
| `notification-api` | Consumes task events and processes task notification delivery |
| `docker-compose.yml` | Provides local infrastructure dependencies for development |

## Main Features

- Customer registration and profile management
- Authenticated customer operations using `X-User-Email`
- Admin customer and task management
- Task creation, update, listing, filtering, and soft deletion
- Pending task search by date period
- Notification status updates
- Kafka-based asynchronous notification flow
- Eureka service discovery
- Spring Cloud Config centralized configuration
- Gateway-level OAuth2 Resource Server security with JWT validation
- Gateway route protection for public, authenticated, and admin APIs
- Redis-backed request rate limiting by authenticated JWT subject
- Resilience4j circuit breakers with gateway fallback routes
- RabbitMQ support for Spring Cloud Bus refresh
- Environment-specific configuration profiles
- GitHub Actions CI workflow for microservice builds

## Tech Stack

- Java 17
- Spring Boot
- Spring Cloud Gateway
- Spring Cloud Config Server
- Netflix Eureka
- Spring Security OAuth2 Resource Server
- Keycloak
- PostgreSQL
- MongoDB
- Kafka
- RabbitMQ
- Redis
- Resilience4j
- Docker Compose
- Gradle
- JUnit 5
- Mockito
- WebTestClient
- Testcontainers
- OkHttp MockWebServer
- GitHub Actions

## Project Structure

```text
schedulingtasks-app-ms
|-- .github
|   `-- workflows
|       `-- ci.yml
|-- configserver
|   `-- src/main/resources/config
|       |-- gateway.yaml
|       |-- notification-api.yaml
|       |-- notification-api-dev.yaml
|       |-- notification-api-prod.yaml
|       |-- registration-api.yaml
|       |-- registration-api-dev.yaml
|       |-- registration-api-prod.yaml
|       |-- scheduling-api.yaml
|       |-- scheduling-api-dev.yaml
|       `-- scheduling-api-prod.yaml
|-- eureka
|-- gateway
|-- notification-api
|-- registration-api
|-- scheduling-api
`-- docker-compose.yml
```

## API Overview

All application APIs use the base path:

```http
/api/v1
```

### Registration API

Customer endpoints:

```http
POST   /customers
GET    /customers/me
PUT    /customers/me
DELETE /customers/me
POST   /customers/me/addresses
PUT    /customers/me/addresses/{addressId}
POST   /customers/me/phones
PUT    /customers/me/phones/{phoneId}
```

Admin endpoints:

```http
GET    /admin/customers
GET    /admin/customers?email={email}
GET    /admin/customers/{customerId}
DELETE /admin/customers/{customerId}
PATCH  /admin/customers/{customerId}/status
```

### Scheduling API

Customer task endpoints:

```http
POST   /customers/me/tasks
GET    /customers/me/tasks
GET    /customers/me/tasks?initialDate={date}&finalDate={date}&status=PENDING
PUT    /customers/me/tasks/{taskId}
DELETE /customers/me/tasks/{taskId}
```

Admin task endpoints:

```http
GET    /admin/tasks
GET    /admin/customers/{customerId}/tasks
DELETE /admin/customers/{customerId}/tasks/{taskId}
PATCH  /admin/customers/{customerId}/tasks/{taskId}/status
```

## Gateway

The gateway is the single entry point for external clients. It routes requests to services registered in Eureka using load-balanced service names such as:

```text
lb://SCHEDULING-API
lb://REGISTRATION-API
```

Gateway responsibilities include:

- Validating bearer tokens as an OAuth2 Resource Server
- Trusting JWTs issued by the configured Keycloak realm
- Routing registration and scheduling requests
- Applying Redis-backed rate limiting to scheduling task routes
- Resolving rate-limit keys from the JWT `sub` claim
- Falling back to `anonymous` when no authenticated principal exists
- Applying circuit breakers for scheduling and registration routes
- Exposing gateway, health, info, env, and metrics actuator endpoints

Example JWT issuer configuration:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8443/realms/gateway
```

Example scheduling route protections:

```yaml
filters:
  - name: RequestRateLimiter
    args:
      key-resolver: "#{@userKeyResolver}"
      redis-rate-limiter.replenishRate: 10
      redis-rate-limiter.burstCapacity: 20
  - name: CircuitBreaker
    args:
      name: schedulingApiCircuitBreaker
      fallbackUri: forward:/fallback/scheduling-api
```

## Testing

The gateway includes both unit and integration tests.

### Unit and Spring Context Tests

| Test | Purpose |
| --- | --- |
| `KeyResolverConfigTest` | Verifies that the `userKeyResolver` returns the JWT subject for authenticated users |
| `KeyResolverConfigTest` | Verifies that the resolver returns `anonymous` when no principal exists |
| `KeyResolverConfigSpringTest` | Verifies that the `userKeyResolver` bean loads correctly in the Spring context |
| `GatewayApplicationTests` | Verifies that the gateway application context loads |

### Gateway Rate Limiter Integration Test

`GatewayRateLimiterIntegrationTest` verifies the gateway rate limiter with a real Redis container and a test downstream route.

It uses:

- `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- Testcontainers with `redis:7-alpine`
- `WebTestClient` for HTTP requests
- A test `ReactiveJwtDecoder` that creates JWTs from test bearer tokens
- A test route for `/api/v1/customers/me/tasks`
- `RedisRateLimiter(1, 1, 1)` to make rate-limit behavior easy to assert

Covered scenarios:

- The first request from a JWT user is allowed
- The second request from the same JWT user returns `429 Too Many Requests`
- Different JWT users are rate limited separately

Run gateway tests:

```bash
cd gateway
./gradlew test
```

On Windows:

```bash
cd gateway
gradlew test
```

## Continuous Integration

The repository includes a GitHub Actions workflow at:

```text
.github/workflows/ci.yml
```

The workflow runs on pushes and pull requests to `master`. It uses a matrix build for:

```text
registration-api
scheduling-api
notification-api
```

Current CI steps:

- Check out the repository
- Set up Java 17 with Temurin
- Grant execute permission to each service Gradle wrapper
- Build each service with Gradle

Current build command:

```bash
./gradlew build -x test --no-daemon
```

Gateway tests are implemented in the repository and can be run locally. A future CI improvement would be to include the `gateway` service in the matrix and enable test execution in the workflow.

## Configuration

Configuration is centralized in the `configserver` under:

```text
configserver/src/main/resources/config
```

The config server runs on:

```http
http://localhost:8888
```

Example config lookup:

```http
http://localhost:8888/scheduling-api/dev
```

Configuration files include shared and profile-specific versions for each service:

```text
gateway.yaml
registration-api.yaml
registration-api-dev.yaml
registration-api-prod.yaml
scheduling-api.yaml
scheduling-api-dev.yaml
scheduling-api-prod.yaml
notification-api.yaml
notification-api-dev.yaml
notification-api-prod.yaml
```

## Local Development

### Prerequisites

- Java 17+
- Docker and Docker Compose
- Gradle

### Start Infrastructure

From the root folder:

```bash
docker-compose up -d
```

### Start Services

Recommended startup order:

```text
1. configserver
2. eureka
3. gateway
4. registration-api
5. scheduling-api
6. notification-api
```

Each service can be started from its own folder using Gradle:

```bash
./gradlew bootRun
```

On Windows:

```bash
gradlew bootRun
```

## Default Ports

| Service | Port |
| --- | ---: |
| Config Server | `8888` |
| Eureka Server | `8761` |
| Gateway | `8880` |
| Registration API | `8082` |
| Scheduling API | `8084` |
| Notification API | Configured by profile |

## Example Request Flow

### Creating a task

```text
Client
  -> Gateway validates JWT and applies rate limiting
  -> Gateway routes to Scheduling API through Eureka
  -> Scheduling API creates the task
  -> Scheduling API publishes a task event to Kafka
  -> Notification API consumes the task event
  -> Notification API processes the notification
```

The gateway forwards authenticated user context with:

```http
X-User-Email: customer@email.com
```

The downstream services use this header to execute customer-specific operations without exposing customer IDs in the public API.

## Portfolio Highlights

This project demonstrates:

- Microservice decomposition by business capability
- Centralized configuration with Spring Cloud Config
- Service discovery with Eureka
- API gateway routing with Spring Cloud Gateway
- Keycloak/OAuth2 JWT resource-server security
- Redis-backed rate limiting by authenticated user
- Circuit breaker and fallback configuration with Resilience4j
- Customer and admin route separation
- Event-driven communication with Kafka
- Database-per-service design using PostgreSQL and MongoDB
- Async notification processing
- Gateway unit testing and integration testing
- Testcontainers-based infrastructure testing
- Docker-based local infrastructure
- GitHub Actions CI for service builds
- Environment-specific configuration management

## Future Improvements

- Add OpenAPI/Swagger documentation
- Include gateway in the GitHub Actions matrix
- Enable test execution in CI instead of skipping tests
- Add centralized logging with ELK or OpenSearch
- Add distributed tracing with Zipkin or Tempo
- Add Kubernetes deployment manifests
- Add Testcontainers integration tests for registration, scheduling, and notification services

## Author

Developed by Grazielle Anaia.


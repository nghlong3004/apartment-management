# Apartment Management

RESTful API service for managing apartments, floors, rooms, and users. Built with Spring Boot, secured with JWT, connected to PostgreSQL using MyBatis, documented with OpenAPI/Swagger, and monitored with Micrometer/Prometheus.

## Features

- User authentication with JWT (access + refresh tokens)
- User roles and authorization
- CRUD for floors and rooms
- Assign/update floor managers and room owners
- Validation and global exception handling
- Pagination support for listings
- OpenAPI 3 / Swagger UI for API docs
- Metrics and health checks via Micrometer + Prometheus

## Tech Stack

- Java 21
- Spring Boot 3.5.4
- PostgreSQL
- MyBatis 
- JWT 
- springdoc-openapi
- Micrometer + Prometheus
- Maven

## Project Structure

- **controller**: REST API controllers (Auth, User, Floor, Room, FloorRequest)
- **service**: business logic
- **repository**: MyBatis mappers
- **model**: entities, enums, DTOs
- **security**: JWT filters, config
- **exception**: custom exceptions and global handler
- **docs**: project documentation (Apartment_Architecture_System.pdf)

## Configuration

All configs in `application-dev.properties`:

```properties
spring.datasource.url=jdbc:${DBMS}://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

jwt.access-token-expiration-ms=900000
jwt.refresh-token-expiration-ms=604800000
jwt.secret.key=your-secret${JWT_SECRET}

apartment.management.floor.max-number=10000
apartment.management.room.max-number=100

mybatis.configuration.map-underscore-to-camel-case=true
mybatis.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl
```

Prometheus metrics exposed on management port:

```properties
management.endpoints.web.exposure.include=health,prometheus,swagger-ui,openapi
management.server.port=${MANAGEMENT_SERVER_PORT}
```

## Running Locally

1. Clone the repository
   ```bash
   git clone https://github.com/nghlong3004/apartment-management.git
   cd apartment-management
   ```
2. Configure database and environment variables
3. Build and run
   ```bash
   mvn clean package
   mvn spring-boot:run
   ```

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## API Endpoints

Base path: `/api/v1`

### Auth

- `POST /auth/register` – register new user
- `POST /auth/login` – login and get JWT

### Floors

- `POST /floors` – create floor
- `GET /floors` – list floors (paged)
- `GET /floors/{id}` – get floor detail
- `PUT /floors/{id}` – update floor
- `DELETE /floors/{id}` – delete floor
- `PUT /floors/{id}/manager` – set manager

### Rooms

- `POST /floors/{floorId}/rooms` – create room
- `GET /floors/{floorId}/rooms` – list rooms (paged)
- `GET /floors/{floorId}/rooms/{roomId}` – get room detail
- `PUT /floors/{floorId}/rooms/{roomId}` – update room
- `DELETE /floors/{floorId}/rooms/{roomId}` – delete room
- `PUT /floors/{floorId}/rooms/{roomId}/owner` – set room owner

## Documentation
Additional diagrams and documentation can be found in the `docs`/ folder, including:
- `Apartment_Architecture_System.pdf`: overview of the system architecture
## Testing

Run unit and integration tests:

```bash
mvn test
```


# HelpFlow — Prueba de Desempeño 

Sistema de gestión de tickets de soporte técnico con arquitectura de microservicios y arquitectura hexagonal.

## Servicios

| Servicio | Puerto | Descripción |
|---|---|---|
| `support-ticket-service` | 8080 | API principal con gestión de tickets, usuarios y técnicos |
| `technician-dispatch-service` | 8081 | Servicio de asignación de técnicos (simulación determinista) |
| PostgreSQL | 5432 | Base de datos relacional |

## Requisitos

- Java 17+
- Maven 3.9+
- Docker & Docker Compose

## Inicio rápido con Docker Compose

```bash
# Copia el archivo de variables de entorno
cp .env.example .env

# Edita el .env y cambia JWT_SECRET por un valor seguro
# Luego levanta todos los servicios
docker-compose up --build
```

## Desarrollo local

### technician-dispatch-service
```bash
cd technician-dispatch-service
mvn spring-boot:run
# Disponible en http://localhost:8081
# Swagger UI: http://localhost:8081/swagger-ui.html
```

### support-ticket-service
Requiere PostgreSQL corriendo. Puedes levantar solo la DB con:
```bash
docker-compose up db -d
```

```bash
cd support-ticket-service
export JWT_SECRET=change-this-to-a-long-random-secret-at-least-32-characters
mvn spring-boot:run
# Disponible en http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

## Usuarios de prueba (seed en V3__datos_iniciales.sql)

| Email | Password | Rol |
|---|---|---|
| `admin@helpflow.com` | `Admin1234!` | ROLE_ADMIN |
| `tecnico@helpflow.com` | `Tech1234!` | ROLE_TECNICO |
| `usuario@helpflow.com` | `User1234!` | ROLE_USUARIO |

## Endpoints principales

### Autenticación
- `POST /auth/register` — Registro de nuevo usuario
- `POST /auth/login` — Login y obtención de JWT

### Tickets
- `POST /api/tickets` — Crear ticket (USUARIO, ADMIN)
- `GET /api/tickets/my` — Mis tickets (USUARIO, ADMIN)
- `GET /api/tickets` — Todos los tickets (ADMIN)
- `GET /api/tickets/{id}` — Detalle de ticket
- `PATCH /api/tickets/{id}/status` — Cambiar estado (TECNICO, ADMIN)

### Usuarios y Técnicos
- `GET /api/users` — Listar usuarios (ADMIN)
- `GET /api/technicians` — Listar técnicos (ADMIN)

### Dispatch
- `POST /technician-dispatch` — Asignar técnico (servicio interno)

## Arquitectura

### support-ticket-service — Arquitectura Hexagonal
```
domain/          → Modelos puros y excepciones (sin dependencias de framework)
application/     → Casos de uso (puertos de entrada/salida) y servicios
infrastructure/  → Adaptadores JPA, REST, seguridad JWT, dispatch HTTP
```

### SLA por prioridad
| Prioridad | SLA |
|---|---|
| BAJA | 72 horas |
| MEDIA | 24 horas |
| ALTA | 8 horas |
| CRÍTICA | 2 horas |

## Ejecutar tests

```bash
# Tests unitarios (sin Docker)
cd support-ticket-service
mvn test -Dtest="CreateTicketServiceTest,TicketTransitionTest"

# Tests de integración (requiere Docker para Testcontainers)
mvn verify
```

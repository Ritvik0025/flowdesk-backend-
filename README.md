# FlowDesk Backend

A production-ready REST API backend for a real-time task and team collaboration platform. Built with Java 21 and Spring Boot 3.5, featuring JWT authentication, Redis caching, Kafka event streaming, and PostgreSQL — all containerized with Docker.

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Backend language |
| Spring Boot | 3.5 | REST API framework |
| Spring Security | 6.x | Authentication & authorization |
| PostgreSQL | 15 | Primary database |
| Redis | 7 | Token caching & session management |
| Apache Kafka | 7.4 | Event streaming |
| Docker | Latest | Containerization |
| JWT (jjwt) | 0.11.5 | Stateless authentication |
| Maven | 3.9 | Build tool |

---

## Features

- **JWT Authentication** — Secure register and login with BCrypt password hashing and JWT token generation
- **Token Management** — Refresh tokens stored in Redis with automatic 24-hour expiry
- **Project Management** — Full CRUD for projects with user ownership
- **Task Management** — Create, assign, move, and delete tasks with priority levels
- **Kanban Support** — Tasks move between TODO, IN_PROGRESS, and DONE statuses
- **Event Streaming** — Every task action publishes a Kafka event (created, updated, deleted)
- **Role-based Access** — Protected routes require valid JWT token
- **CORS Configured** — Ready to connect with React frontend

---

## Architecture

```
HTTP Request
      ↓
Controller       ← receives request, validates input
      ↓
Service          ← business logic, Redis, Kafka calls
      ↓
Repository       ← database queries via Spring Data JPA
      ↓
PostgreSQL       ← persistent storage
```

---

## Project Structure

```
src/main/java/com/flowdesk/flowdesk_backend/
├── config/
│   ├── SecurityConfig.java       ← Spring Security + CORS setup
│   ├── JwtUtil.java              ← Token generation and validation
│   ├── JwtAuthFilter.java        ← Intercepts every request, validates token
│   ├── RedisConfig.java          ← Redis template configuration
│   ├── KafkaProducer.java        ← Publishes task events to Kafka
│   └── KafkaConsumer.java        ← Listens and processes task events
│
├── controller/
│   ├── AuthController.java       ← POST /api/auth/register, /api/auth/login
│   ├── ProjectController.java    ← GET/POST/DELETE /api/projects
│   └── TaskController.java       ← GET/POST/PUT/DELETE /api/tasks
│
├── service/
│   ├── AuthService.java          ← Register, login, token logic
│   ├── ProjectService.java       ← Project business logic
│   ├── TaskService.java          ← Task logic + Kafka event publishing
│   └── RedisService.java         ← Save, get, delete from Redis
│
├── repository/
│   ├── UserRepository.java       ← findByEmail, existsByEmail
│   ├── ProjectRepository.java    ← findByOwner
│   └── TaskRepository.java       ← findByProject
│
├── model/
│   ├── User.java                 ← users table (id, name, email, password, plan)
│   ├── Project.java              ← projects table (id, name, description, owner_id)
│   └── Task.java                 ← tasks table (id, title, status, priority, project_id, assignee_id)
│
└── dto/
    ├── RegisterRequest.java      ← name, email, password
    ├── LoginRequest.java         ← email, password
    └── AuthResponse.java         ← token, email, name
```

---

## API Endpoints

### Auth (Public — No token required)

| Method | Endpoint | Body | Response |
|---|---|---|---|
| POST | `/api/auth/register` | `{ name, email, password }` | `{ token, email, name }` |
| POST | `/api/auth/login` | `{ email, password }` | `{ token, email, name }` |

### Projects (Protected — JWT token required)

| Method | Endpoint | Body | Response |
|---|---|---|---|
| GET | `/api/projects` | — | List of user's projects |
| POST | `/api/projects` | `{ name, description }` | Created project |
| DELETE | `/api/projects/{id}` | — | 200 OK |

### Tasks (Protected — JWT token required)

| Method | Endpoint | Body | Response |
|---|---|---|---|
| GET | `/api/tasks/{projectId}` | — | List of tasks |
| POST | `/api/tasks/{projectId}` | `{ title, description, priority, assigneeEmail }` | Created task |
| PUT | `/api/tasks/{taskId}/status` | `{ status }` | Updated task |
| DELETE | `/api/tasks/{taskId}` | — | 200 OK |

**Priority values:** `LOW`, `MEDIUM`, `HIGH`

**Status values:** `TODO`, `IN_PROGRESS`, `DONE`

---

## Database Schema

```sql
users
  id            BIGSERIAL PRIMARY KEY
  name          VARCHAR
  email         VARCHAR UNIQUE
  password      VARCHAR (BCrypt hashed)
  plan          VARCHAR (FREE / PRO)
  created_at    TIMESTAMP

projects
  id            BIGSERIAL PRIMARY KEY
  name          VARCHAR
  description   VARCHAR
  owner_id      FK → users.id
  created_at    TIMESTAMP

tasks
  id            BIGSERIAL PRIMARY KEY
  title         VARCHAR
  description   VARCHAR
  status        VARCHAR (TODO / IN_PROGRESS / DONE)
  priority      VARCHAR (LOW / MEDIUM / HIGH)
  project_id    FK → projects.id
  assignee_id   FK → users.id
  created_at    TIMESTAMP
  updated_at    TIMESTAMP
```

---

## How JWT Authentication Works

```
1. User sends POST /api/auth/login with email + password
2. Spring Security validates credentials against database
3. JWT token generated with email as subject, expires in 24 hours
4. Token saved in Redis: key = "token:email", TTL = 1440 minutes
5. Token returned to frontend

6. Frontend sends every request with header:
   Authorization: Bearer <token>

7. JwtAuthFilter intercepts request
8. Extracts and validates token signature and expiry
9. Valid → request proceeds | Invalid → 401 Unauthorized
```

---

## How Kafka Event Streaming Works

Every task action triggers a Kafka event:

```
Task created / updated / deleted
        ↓
TaskService calls KafkaProducer
        ↓
Event published to topic: "task.events"
Format: "TASK_CREATED:taskId:projectId"
        ↓
KafkaConsumer receives event
        ↓
Processes and logs the event
(Can be extended to send notifications, update analytics, etc.)
```

---

## How Redis Caching Works

```
User logs in
        ↓
Token saved: redis.set("token:ritvik@gmail.com", "<jwt>", 1440 mins)
        ↓
Token auto-expires after 24 hours
        ↓
User logs out → redis.delete("token:ritvik@gmail.com")
```

---

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9
- Docker Desktop

### 1. Clone the repository

```bash
git clone https://github.com/Ritvik0025/flowdesk-backend-.git
cd flowdesk-backend
```

### 2. Start infrastructure with Docker

```bash
docker-compose up -d
```

This starts:
- PostgreSQL on port `5432`
- Redis on port `6379`
- Kafka on port `9092`
- Zookeeper on port `2181`

### 3. Run the application

```bash
./mvnw spring-boot:run
```

App starts on `http://localhost:8081`

### 4. Test the API

Register a user:
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John","email":"john@gmail.com","password":"123456"}'
```

Login:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@gmail.com","password":"123456"}'
```

---

## Connecting to Cloud Database

To use a cloud PostgreSQL instead of local Docker, update `application.properties`:

**Supabase (Free):**
```properties
spring.datasource.url=jdbc:postgresql://db.xxxx.supabase.co:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
```

**Railway (Free):**
```properties
spring.datasource.url=jdbc:postgresql://your-railway-url:5432/railway
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
```

Tables are created automatically on first run.

---

## Environment Variables (for production)

Instead of hardcoding in `application.properties`, set these as environment variables:

```
DB_URL=jdbc:postgresql://localhost:5432/flowdesk
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=your-secret-key
REDIS_HOST=localhost
KAFKA_SERVERS=localhost:9092
```

---

## Frontend Repository

The React + TypeScript frontend for this project:
[https://github.com/Ritvik0025/flowdesk](https://github.com/Ritvik0025/flowdesk)

---

## Author

**Ritvik Sharma**
- GitHub: [@Ritvik0025](https://github.com/Ritvik0025)

---

## License

MIT License — feel free to use this project for learning and interviews.

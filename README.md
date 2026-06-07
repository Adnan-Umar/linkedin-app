# 🔗 LinkedInApp — Microservices Social Network Platform

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.0-brightgreen?style=for-the-badge&logo=spring&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-7.6.0-black?style=for-the-badge&logo=apachekafka&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql&logoColor=white)
![Neo4j](https://img.shields.io/badge/Neo4j-Graph%20DB-008CC1?style=for-the-badge&logo=neo4j&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-K8s-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)

**A production-grade LinkedIn-inspired social networking platform built with Spring Boot microservices, event-driven architecture, and cloud-native deployment.**

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture Diagram](#-architecture-diagram)
- [Microservices](#-microservices)
- [Technology Stack](#-technology-stack)
- [API Reference](#-api-reference)
- [Data Flow & Event Diagrams](#-data-flow--event-diagrams)
- [Database Design](#-database-design)
- [Security & Authentication](#-security--authentication)
- [Kafka Messaging](#-kafka-messaging)
- [Service Communication](#-service-communication)
- [Docker Setup](#-docker-setup)
- [Kubernetes Deployment](#-kubernetes-deployment)
- [Project Structure](#-project-structure)
- [Configuration](#-configuration)

---

## 🌟 Overview

LinkedInApp is a **full-featured social networking backend** modeled after LinkedIn. It is built using a **microservices architecture** with 7 independent Spring Boot services that communicate via REST (OpenFeign) and asynchronous events (Apache Kafka).

### Key Features

| Feature | Description |
|---|---|
| 🔐 **JWT Authentication** | Stateless auth with HMAC-SHA signed tokens, validated at the gateway |
| 👤 **User Management** | Signup, login with BCrypt password hashing |
| 📝 **Posts & Likes** | Create posts, like/unlike with fan-out notifications |
| 🤝 **Social Graph** | Send, accept, reject connection requests using Neo4j graph DB |
| 🔔 **Real-time Notifications** | Event-driven notifications via Apache Kafka |
| 📁 **File Uploads** | Media uploads to Cloudinary CDN |
| 🌐 **API Gateway** | Single entry point with auth filter and Eureka load balancing |
| 🐳 **Containerized** | Full Docker Compose and Kubernetes support |

---

## 🏗️ Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              CLIENT (Browser / Mobile)                  │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │  HTTP Requests
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          NGINX INGRESS (K8s)                            │
│                     path: / → api-gateway:80                            │
│                     path: /kafka-ui → kafka-ui:8080                     │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        API GATEWAY  :8080                               │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │  AuthenticationFilter (JWT Validation)                           │   │
│  │  • Validates Bearer token from Authorization header              │   │
│  │  • Extracts userId → injects X-User-Id header downstream        │   │
│  │  • Returns 401 if token is missing / invalid                     │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  Routes:  /api/v1/users/**      → USER-SERVICE        (no auth)         │
│           /api/v1/posts/**      → POSTS-SERVICE       (auth required)   │
│           /api/v1/connections/** → CONNECTIONS-SERVICE (auth required)  │
└──────────┬──────────────────────────────────────────────────────────────┘
           │  Service Discovery via Eureka (Docker) / K8s DNS (K8s)
           │
     ┌─────┴─────────────────────────────────────────┐
     │                                               │
     ▼                                               ▼
┌──────────────┐    ┌────────────────┐    ┌──────────────────────┐
│ USER-SERVICE │    │  POSTS-SERVICE │    │ CONNECTIONS-SERVICE  │
│   :9020      │    │     :9010      │    │       :9030          │
│              │    │                │    │                      │
│ • signup     │    │ • createPost   │    │ • sendRequest        │
│ • login      │    │ • likePost     │    │ • acceptRequest      │
│ • JWT issue  │    │ • getPost      │    │ • rejectRequest      │
│              │    │                │    │ • getConnections     │
│  [PostgreSQL]│    │  [PostgreSQL]  │    │    [Neo4j Graph DB]  │
│   userDB     │    │   postsDB      │    │                      │
└──────────────┘    └───────┬────────┘    └──────────┬───────────┘
                            │                        │
                            │  Kafka Events          │ Kafka Events
                            ▼                        ▼
                  ┌─────────────────────────────────────────────┐
                  │              APACHE KAFKA                   │
                  │                                             │
                  │  Topics:                                    │
                  │  • post-created-topic       (3 partitions)  │
                  │  • post-liked-topic         (3 partitions)  │
                  │  • send-connection-request  (3 partitions)  │
                  │  • accept-connection-request (3 partitions) │
                  └────────────────────┬────────────────────────┘
                                       │ Consumes all 4 topics
                                       ▼
                          ┌──────────────────────────┐
                          │  NOTIFICATION-SERVICE    │
                          │         :9040            │
                          │                          │
                          │ • Handles all events     │
                          │ • Persists notifications │
                          │ • Fan-out to connections │
                          │                          │
                          │     [PostgreSQL]         │
                          │    notificationDB        │
                          └──────────────────────────┘

                  ┌────────────────────────────────────┐
                  │         DISCOVERY SERVER           │
                  │    (Netflix Eureka)  :8761         │
                  │  All services register here        │
                  │  (Docker mode only)                │
                  └────────────────────────────────────┘

                  ┌────────────────────────────────────┐
                  │        UPLOADER-SERVICE            │
                  │             :9050                  │
                  │  File upload → Cloudinary CDN      │
                  └────────────────────────────────────┘
```

---

## 🧩 Microservices

### 1. 🔍 Discovery Server

> **Port:** `8761` | **Role:** Service Registry

Netflix Eureka server that all services register with in Docker/local mode. Acts as the service catalog enabling load-balanced inter-service communication via `lb://SERVICE-NAME` URIs.

```
@EnableEurekaServer — runs standalone (does not self-register)
```

---

### 2. 🚪 API Gateway

> **Port:** `8080` | **Role:** Single entry point, JWT validation, routing

The reactive Spring Cloud Gateway (WebFlux) that sits in front of all services. It is the **only service exposed externally**.

**Key Responsibilities:**
- Validates `Authorization: Bearer <token>` header
- Extracts `userId` from JWT and injects `X-User-Id` header for downstream services
- Routes requests to the correct microservice via Eureka load balancing (Docker) or K8s DNS
- Returns `HTTP 401` for missing, malformed, or expired tokens

**Routing Table:**

| Path Pattern | Target Service | Auth Required |
|---|---|---|
| `/api/v1/users/**` | USER-SERVICE | ❌ No |
| `/api/v1/posts/**` | POSTS-SERVICE | ✅ Yes |
| `/api/v1/connections/**` | CONNECTIONS-SERVICE | ✅ Yes |

> Note: The gateway strips `/api/v1` prefix before forwarding using `StripPrefix=2`.

---

### 3. 👤 User Service

> **Port:** `9020` | **Context:** `/users` | **DB:** PostgreSQL (`userDB`)

Handles user registration, authentication, and JWT token issuance.

**Components:**

| Class | Responsibility |
|---|---|
| `AuthController` | REST endpoints `/auth/signup`, `/auth/login` |
| `AuthService` | Business logic: validate, hash password, call JwtService |
| `JwtService` | JJWT 0.13.0 — generates tokens (userId as subject, 100 min expiry) |
| `User` | JPA Entity: `id`, `name`, `email`, `password` |
| `UserRepository` | `findByEmail()`, `existsByEmail()` |
| `PasswordUtil` | BCrypt hash and verify via jbcrypt 0.4 |
| `GlobalExceptionHandler` | Returns proper HTTP error codes |

---

### 4. 📝 Posts Service

> **Port:** `9010` | **Context:** `/posts` | **DB:** PostgreSQL (`postsDB`)

Manages post creation, retrieval, and the like/unlike system. Also triggers Kafka events and calls Connections Service via Feign.

**Components:**

| Class | Responsibility |
|---|---|
| `PostsController` | CRUD endpoints for posts |
| `LikesController` | Like / unlike endpoints |
| `PostsService` | Creates post, emits `PostCreatedEvent` to Kafka |
| `PostLikeService` | Like/unlike logic, emits `PostLikedEvent` to Kafka |
| `ConnectionsClient` | Feign client → fetches user's first-degree connections |
| `FeignClientInterceptor` | Propagates `X-User-Id` header on Feign calls |
| `UserContextHolder` | ThreadLocal storage for authenticated user ID |
| `UserInterceptor` | MVC interceptor — reads `X-User-Id` from gateway header |
| `KafkaTopicConfig` | Creates `post-created-topic` and `post-liked-topic` |

---

### 5. 🤝 Connections Service

> **Port:** `9030` | **Context:** `/connections` | **DB:** Neo4j Graph Database

Manages the social graph — who is connected to whom. Uses **Neo4j** for efficient graph traversal queries (perfect for friend-of-friend lookups).

**Graph Model:**

```
(Person)-[:REQUESTED_TO]->(Person)    ← Pending connection request
(Person)-[:CONNECTED_TO]-(Person)     ← Accepted connection (bidirectional)
```

**Neo4j Cypher Queries:**

```cypher
-- Get 1st-degree connections
MATCH (personA:Person)-[:CONNECTED_TO]-(personB:Person)
WHERE personA.userId = $userId
RETURN personB

-- Accept request (delete REQUESTED_TO, create CONNECTED_TO)
MATCH (p1:Person)-[r:REQUESTED_TO]->(p2:Person)
WHERE p1.userId = $senderId AND p2.userId = $receiverId
DELETE r
CREATE (p1)-[:CONNECTED_TO]->(p2)
```

**Components:**

| Class | Responsibility |
|---|---|
| `ConnectionsController` | REST endpoints for connection CRUD |
| `ConnectionsService` | Graph operations + Kafka event publishing |
| `Person` | Neo4j `@Node`: `id`, `userId`, `name` |
| `PersonRepository` | Custom Cypher queries via Spring Data Neo4j |
| `KafkaTopicConfig` | Creates `send-connection-request-topic`, `accept-connection-request-topic` |

---

### 6. 🔔 Notification Service

> **Port:** `9040` | **DB:** PostgreSQL (`notificationDB`) | **Role:** Kafka consumer

Pure event-driven service. Listens to **4 Kafka topics** and persists notifications to the database. Uses Feign to call Connections Service for post notification fan-out.

**Kafka Consumers:**

| Topic | Handler | Action |
|---|---|---|
| `send-connection-request-topic` | `ConnectionsServiceConsumer` | Notify receiver of incoming request |
| `accept-connection-request-topic` | `ConnectionsServiceConsumer` | Notify sender their request was accepted |
| `post-created-topic` | `PostsServiceConsumer` | Fetch all connections → notify each one |
| `post-liked-topic` | `PostsServiceConsumer` | Notify post creator about the like |

**Components:**

| Class | Responsibility |
|---|---|
| `ConnectionsServiceConsumer` | Listens to connection events |
| `PostsServiceConsumer` | Listens to post events, calls Feign for fan-out |
| `SendNotification` | Persists notification record to DB |
| `Notification` | JPA Entity: `id`, `userId`, `message`, `createdAt` |
| `ConnectionsClient` | Feign client → first-degree connections |

---

### 7. 📁 Uploader Service

> **Port:** `9050` | **External:** Cloudinary CDN

Handles media file uploads. Accepts multipart file uploads and stores them on Cloudinary, returning the secure CDN URL.

**Components:**

| Class | Responsibility |
|---|---|
| `FileUploadController` | `POST /file` — accepts `MultipartFile` |
| `CloudinaryFileUploaderService` | Uploads bytes to Cloudinary, returns `secure_url` |
| `FileUploaderConfig` | Configures `@Bean Cloudinary` from env vars |

**Required Environment Variables:**
```
CLOUDINARY_CLOUD_NAME=<your-cloud-name>
CLOUDINARY_API_KEY=<your-api-key>
CLOUDINARY_SECRET_KEY=<your-secret-key>
```

---

## 💻 Technology Stack

| Category | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 4.0.2 |
| Service Mesh | Spring Cloud | 2025.1.0 |
| API Gateway | Spring Cloud Gateway (WebFlux) | 2025.1.0 |
| Service Discovery | Netflix Eureka | — |
| HTTP Client | OpenFeign | — |
| Auth | JJWT (JWT) | 0.13.0 |
| Password Hashing | jbcrypt | 0.4 |
| Messaging | Apache Kafka (Confluent) | 7.6.0 |
| Relational DB | PostgreSQL | 16 |
| Graph DB | Neo4j | Latest |
| ORM | Spring Data JPA / Hibernate | — |
| Graph ORM | Spring Data Neo4j | — |
| File Storage | Cloudinary SDK | — |
| Build Tool | Maven | — |
| Image Build | Google Jib Maven Plugin | 3.4.4 |
| Containerization | Docker + Docker Compose | — |
| Orchestration | Kubernetes + Nginx Ingress | — |
| Utilities | Lombok, ModelMapper | 3.2.6 |
| Monitoring | Kafbat UI, Spring Actuator | — |

---

## 📡 API Reference

All requests go through the API Gateway at `http://localhost:8080`.

### 🔑 Authentication

#### `POST /api/v1/users/auth/signup`

Register a new user.

```json
// Request Body
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securepassword"
}

// Response 201 Created
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

#### `POST /api/v1/users/auth/login`

Authenticate and receive a JWT token.

```json
// Request Body
{
  "email": "john@example.com",
  "password": "securepassword"
}

// Response 200 OK
"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

> Use the returned token as: `Authorization: Bearer <token>` in all subsequent requests.

---

### 📝 Posts

#### `POST /api/v1/posts/core`

Create a new post. *(Auth required)*

```json
// Request Body
{
  "content": "Hello LinkedIn world!"
}

// Response 201 Created
{
  "id": 1,
  "content": "Hello LinkedIn world!",
  "userId": 1,
  "createdAt": "2024-01-01T10:00:00"
}
```

#### `GET /api/v1/posts/core/{postId}`

Get a post by ID. *(Auth required)*

```
Response 200 OK → PostDto
```

#### `GET /api/v1/posts/core/users/{userId}/allPosts`

Get all posts by a user. *(Auth required)*

```
Response 200 OK → List<PostDto>
```

#### `POST /api/v1/posts/likes/{postId}`

Like a post. *(Auth required)*

```
Response 204 No Content
```

#### `DELETE /api/v1/posts/likes/{postId}`

Unlike a post. *(Auth required)*

```
Response 204 No Content
```

---

### 🤝 Connections

#### `GET /api/v1/connections/core/first-degree`

Get your first-degree connections. *(Auth required)*

```
Response 200 OK → List<Person>
```

#### `POST /api/v1/connections/core/request/{userId}`

Send a connection request to a user. *(Auth required)*

```
Response 200 OK → true
```

#### `POST /api/v1/connections/core/accept/{userId}`

Accept an incoming connection request. *(Auth required)*

```
Response 200 OK → true
```

#### `POST /api/v1/connections/core/reject/{userId}`

Reject an incoming connection request. *(Auth required)*

```
Response 200 OK → true
```

---

### 📁 File Upload

#### `POST /file` *(Uploader Service — port 9050)*

Upload a file to Cloudinary.

```
Content-Type: multipart/form-data
Parameter: file (MultipartFile)

Response 200 OK → "https://res.cloudinary.com/.../secure_url"
```

---

## 🔄 Data Flow & Event Diagrams

### 🔐 Authentication Flow

```
Client                  API Gateway              User Service           Database
  │                         │                        │                     │
  │─── POST /api/v1/users ──►│                        │                     │
  │    /auth/signup          │──── forward (no JWT) ──►│                     │
  │                         │                        │── save User (BCrypt) ►│
  │                         │                        │◄─────────────────────│
  │◄── 201 UserDto ─────────│◄──── UserDto ──────────│                     │
  │                         │                        │                     │
  │─── POST /api/v1/users ──►│                        │                     │
  │    /auth/login           │──── forward (no JWT) ──►│                     │
  │                         │                        │── findByEmail ───────►│
  │                         │                        │◄─────────────────────│
  │                         │                        │── BCrypt.verify()    │
  │                         │                        │── generateJWT()      │
  │◄── 200 JWT Token ───────│◄──── JWT Token ─────────│                     │
```

---

### 📝 Post Creation & Notification Fan-Out

```
Client           API Gateway        Posts Service      Connections Service
   │                  │                  │                      │
   │─ POST /posts ────►│                  │                      │
   │  Authorization:   │── X-User-Id:1 ──►│                      │
   │  Bearer <jwt>     │                  │── UserContextHolder  │
   │                   │                  │   .getCurrentUserId()│
   │                   │                  │── save Post to DB    │
   │                   │                  │                      │
   │                   │                  │── Feign GET ─────────►│
   │                   │                  │  /connections/core    │
   │                   │                  │  /first-degree       │
   │                   │                  │◄── List<Person> ──────│
   │                   │                  │                      │
   │                   │                  │
   │                   │            ┌─────┴────────────────┐
   │                   │            │ Kafka Producer        │
   │                   │            │ topic: post-created   │
   │                   │            │ {postId, creatorId,   │
   │                   │            │  content}             │
   │                   │            └─────────────┬─────────┘
   │◄─ 201 PostDto ────│◄──── PostDto ────────────│
   │                   │                          │
   │                   │                          ▼
   │                   │               ┌──────────────────────┐
   │                   │               │  Notification Service │
   │                   │               │  Kafka Consumer       │
   │                   │               │                       │
   │                   │               │ 1. Get creator's      │
   │                   │               │    connections        │
   │                   │               │    (Feign call)       │
   │                   │               │                       │
   │                   │               │ 2. For each conn:     │
   │                   │               │    save Notification  │
   │                   │               │    to notificationDB  │
   │                   │               └──────────────────────┘
```

---

### 🤝 Connection Request Flow

```
Client           API Gateway     Connections Service    Kafka         Notification Service
   │                  │                  │               │                    │
   │─ POST /request/2 ►│                  │               │                    │
   │  Bearer <jwt>    │── X-User-Id:1 ──►│               │                    │
   │                  │                  │               │                    │
   │                  │                  │─ Check: not same user              │
   │                  │                  │─ Check: request not exists         │
   │                  │                  │─ Check: not already connected      │
   │                  │                  │─ CREATE (p1)-[:REQUESTED_TO]-(p2)  │
   │                  │                  │               │                    │
   │                  │                  │── Publish ───►│                    │
   │                  │                  │  send-conn-   │                    │
   │                  │                  │  request-topic│                    │
   │                  │                  │               │── consume ─────────►│
   │                  │                  │               │                    │── save Notification
   │                  │                  │               │                    │   "You have received
   │◄─ 200 true ──────│◄── true ─────────│               │                    │    a connection
                                                                              │    request from..."
```

---

### 🔑 JWT Header Propagation

```
                    ┌─────────────────────────────────────────┐
                    │              API GATEWAY                │
                    │                                         │
   HTTP Request     │  1. Extract token from                  │
   Authorization: ──►     Authorization header                │
   Bearer eyJ...   │                                         │
                    │  2. Validate JWT signature              │
                    │     (HMAC-SHA, shared secret)           │
                    │                                         │
                    │  3. Extract userId from JWT.subject     │
                    │                                         │
                    │  4. Add header:                         │
                    │     X-User-Id: <userId>                 │
                    │                                         │
                    └──────────────────┬──────────────────────┘
                                       │  Request + X-User-Id header
                                       ▼
                    ┌─────────────────────────────────────────┐
                    │         DOWNSTREAM SERVICE              │
                    │                                         │
                    │  UserInterceptor (MVC Interceptor)      │
                    │  └─ reads X-User-Id from header         │
                    │  └─ stores in ThreadLocal               │
                    │                                         │
                    │  UserContextHolder.getCurrentUserId()   │
                    │  └─ retrieves from ThreadLocal          │
                    │  └─ available throughout request        │
                    └─────────────────────────────────────────┘
```

---

## 🗄️ Database Design

### PostgreSQL — User Service (`userDB`)

```sql
CREATE TABLE users (
    id       BIGSERIAL    PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL   -- BCrypt hashed
);
```

### PostgreSQL — Posts Service (`postsDB`)

```sql
CREATE TABLE posts (
    id         BIGSERIAL    PRIMARY KEY,
    content    TEXT         NOT NULL,
    user_id    BIGINT       NOT NULL,
    created_at TIMESTAMP    DEFAULT NOW()
);

CREATE TABLE post_likes (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    post_id    BIGINT       NOT NULL REFERENCES posts(id),
    created_at TIMESTAMP    DEFAULT NOW(),
    UNIQUE (user_id, post_id)
);
```

### PostgreSQL — Notification Service (`notificationDB`)

```sql
CREATE TABLE notification (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    message    TEXT         NOT NULL,
    created_at TIMESTAMP    DEFAULT NOW()
);
```

### Neo4j — Connections Service (Graph Model)

```
Node: Person
  Properties:
    - id:     Long (internal Neo4j ID)
    - userId: Long (application user ID)
    - name:   String

Relationships:
  (Person)-[:REQUESTED_TO]->(Person)   → Pending connection request
  (Person)-[:CONNECTED_TO]-(Person)    → Accepted bidirectional connection
```

**Graph Visualization:**

```
         [Alice]
        /       \
  CONNECTED   REQUESTED_TO
      /              \
   [Bob]           [Charlie]

Alice and Bob are connected (friends).
Alice has sent a connection request to Charlie (pending).
```

---

## 🔐 Security & Authentication

### JWT Token Details

| Property | Value |
|---|---|
| Algorithm | HMAC-SHA (via JJWT 0.13.0) |
| Subject | `userId` (Long as String) |
| Claims | `email`, `iat`, `exp` |
| Expiry | 100 minutes |
| Library | JJWT 0.13.0 |

### Token Lifecycle

```
1. User calls POST /auth/login
2. user-service validates credentials
3. user-service generates JWT:
   {
     sub: "1",         ← userId
     email: "...",
     iat: <now>,
     exp: <now + 100min>
   }
   Signed with HMAC-SHA secret key

4. Client stores token
5. Client sends: Authorization: Bearer <token>
6. api-gateway validates using same secret key
7. api-gateway extracts userId → forwards X-User-Id header
```

### Public vs Protected Routes

```
PUBLIC  (no JWT needed):
  POST /api/v1/users/auth/signup
  POST /api/v1/users/auth/login

PROTECTED (JWT required):
  ALL /api/v1/posts/**
  ALL /api/v1/connections/**
```

### Password Security

Passwords are hashed using **BCrypt** (jbcrypt 0.4) before storage. The plaintext password is never stored or returned.

---

## 📨 Kafka Messaging

### Topics Summary

| Topic Name | Partitions | Producer | Consumer | Payload |
|---|---|---|---|---|
| `post-created-topic` | 3 | posts-service | notification-service | `PostCreatedEvent {postId, creatorId, content}` |
| `post-liked-topic` | 3 | posts-service | notification-service | `PostLikedEvent {postId, creatorId, likeByUserId}` |
| `send-connection-request-topic` | 3 | connections-service | notification-service | `SendConnectionRequestEvent {senderId, receiverId}` |
| `accept-connection-request-topic` | 3 | connections-service | notification-service | `AcceptConnectionRequestEvent {senderId, receiverId}` |

### Kafka Configuration

```yaml
# Producer (posts-service, connections-service)
kafka:
  bootstrap-servers: kafka:9092
  producer:
    key-serializer: LongSerializer
    value-serializer: JsonSerializer

# Consumer (notification-service)
kafka:
  consumer:
    group-id: notification-service
    key-deserializer: LongDeserializer
    value-deserializer: JsonDeserializer
    trusted-packages: com.adnanumar.linkedin.*
```

### Event Flow Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                    KAFKA EVENT BUS                               │
│                                                                  │
│  ┌─────────────┐     post-created-topic     ┌────────────────┐  │
│  │             │ ─────────────────────────► │                │  │
│  │   POSTS     │     post-liked-topic        │  NOTIFICATION  │  │
│  │   SERVICE   │ ─────────────────────────► │    SERVICE     │  │
│  │             │                            │                │  │
│  └─────────────┘                            │                │  │
│                                             │                │  │
│  ┌─────────────┐  send-conn-request-topic   │                │  │
│  │ CONNECTIONS │ ─────────────────────────► │                │  │
│  │   SERVICE   │  accept-conn-req-topic      │                │  │
│  │             │ ─────────────────────────► │                │  │
│  └─────────────┘                            └────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🔗 Service Communication

### Synchronous (REST via OpenFeign)

```
posts-service ─────────────────────────────────► connections-service
  GET /connections/core/first-degree
  (to get post creator's connections for context)

notification-service ───────────────────────────► connections-service
  GET /connections/core/first-degree
  (to fan-out post-created notifications to all connections)
```

**Feign Client Header Propagation:**

`FeignClientInterceptor` in posts-service automatically propagates the `X-User-Id` header on all outgoing Feign calls so the connections-service can identify the requesting user.

### Asynchronous (Apache Kafka)

All notifications are delivered asynchronously — the producing service publishes an event and immediately returns, while the notification-service processes the event independently.

---

## 🐳 Docker Setup

### Full Stack (All Services)

```bash
docker-compose up -d
```

This starts all 7 services + infrastructure.

### Service Ports (after docker-compose)

| Service | Port | URL |
|---|---|---|
| API Gateway | 8080 | http://localhost:8080 |
| Eureka Dashboard | 8761 | http://localhost:8761 |
| Kafbat UI | 8090 | http://localhost:8090 |
| Neo4j Browser | 7474 | http://localhost:7474 |
| Neo4j Bolt | 7687 | bolt://localhost:7687 |

### Partial Compose Files

| File | Contents |
|---|---|
| `docker-compose.yml` | Full stack (all services + DBs + Kafka) |
| `docker-compose.base.yml` | connections-db, discovery-server, connections-service, api-gateway |
| `docker-compose.notification-kafka.yml` | Kafka + ZooKeeper + notification-db + notification-service |
| `docker-compose.posts-user.yml` | posts-db, user-db, posts-service, user-service |

### Infrastructure Services

```yaml
# Kafka (with Zookeeper)
kafka:    port 9092
zookeeper: internal only

# Databases
user-db:          PostgreSQL 16, volume: user-db-data
posts-db:         PostgreSQL 16, volume: posts-db-data
notification-db:  PostgreSQL 16, volume: notification-db-data
connections-db:   Neo4j,         volume: connections-db-data
```

### Building Docker Images

All services use **Google Jib** — no Dockerfile needed for build push:

```bash
# Build and push image to Docker Hub
cd user-service
mvn package

# Or for all services
cd <service-directory>
mvn package -DskipTests
```

---

## ☸️ Kubernetes Deployment

### Apply All Manifests

```bash
# Apply infrastructure
kubectl apply -f k8s/user-db.yml
kubectl apply -f k8s/posts-db.yml
kubectl apply -f k8s/notification-db.yml
kubectl apply -f k8s/connections-db.yml
kubectl apply -f k8s/kafka.yml
kubectl apply -f k8s/kafkaui.yml

# Apply services
kubectl apply -f k8s/user-service.yml
kubectl apply -f k8s/posts-service.yml
kubectl apply -f k8s/connections-service.yml
kubectl apply -f k8s/notification-service.yml
kubectl apply -f k8s/api-gateway.yml

# Apply ingress
kubectl apply -f k8s/ingress.yml
```

### K8s Architecture

```
Internet
   │
   ▼
┌──────────────────────────────────────────────────────────────┐
│                    NGINX INGRESS                             │
│  path: /          → api-gateway:80                          │
│  path: /kafka-ui  → kafka-ui-service:8080                   │
└─────────────────────────────┬────────────────────────────────┘
                              │
                              ▼
    ┌─────────────────────────────────────────────┐
    │            api-gateway (ClusterIP:80)        │
    │         containerPort: 8080                  │
    │         SPRING_PROFILES_ACTIVE=k8s           │
    └──────────┬─────────────────────┬─────────────┘
               │                     │
    ┌──────────▼──────┐   ┌──────────▼────────────┐
    │  user-service   │   │    posts-service       │
    │  ClusterIP:80   │   │    ClusterIP:80        │
    │  →9020          │   │    →9010               │
    └──────────┬──────┘   └────────────────────────┘
               │
    ┌──────────▼──────┐   ┌─────────────────────────┐
    │  user-db        │   │  connections-service     │
    │  StatefulSet    │   │  ClusterIP:80 →9030      │
    │  PVC: 1Gi       │   └──────────────────────────┘
    └─────────────────┘
```

### K8s vs Docker Differences

| Feature | Docker Compose | Kubernetes |
|---|---|---|
| Service Discovery | Netflix Eureka (`lb://`) | K8s DNS (`http://service-name`) |
| Config Profile | default | `k8s` (via `SPRING_PROFILES_ACTIVE=k8s`) |
| Kafka Mode | Zookeeper-based | KRaft (no Zookeeper, 2-replica StatefulSet) |
| Resource Limits | None | CPU: 200m, RAM: 400Mi (services) |
| Persistence | Named volumes | PersistentVolumeClaims |
| Ingress | Port mapping | Nginx Ingress Controller |

### Resource Limits (K8s)

| Service | CPU Request | CPU Limit | RAM Request | RAM Limit |
|---|---|---|---|---|
| api-gateway | 100m | 200m | 100Mi | 500Mi |
| All other services | 100m | 200m | 100Mi | 400Mi |
| Kafka (each pod) | 100m | 200m | 512Mi | 1Gi |
| Databases | — | — | — | — |

---

## 📁 Project Structure

```
LinkedInApp/
│
├── 📄 docker-compose.yml                    # Full stack compose
├── 📄 docker-compose.base.yml               # Minimal compose
├── 📄 docker-compose.notification-kafka.yml # Kafka + notifications
├── 📄 docker-compose.posts-user.yml         # Posts + user services
│
├── 📂 k8s/                                  # Kubernetes manifests
│   ├── api-gateway.yml
│   ├── user-service.yml
│   ├── posts-service.yml
│   ├── connections-service.yml
│   ├── notification-service.yml
│   ├── user-db.yml
│   ├── posts-db.yml
│   ├── notification-db.yml
│   ├── connections-db.yml
│   ├── kafka.yml
│   ├── kafkaui.yml
│   └── ingress.yml
│
├── 📂 discovery-server/                     # Netflix Eureka Server
│   └── src/main/java/.../DiscoveryServerApplication.java
│
├── 📂 api-gateway/                          # Spring Cloud Gateway
│   └── src/main/java/.../
│       ├── ApiGatewayApplication.java
│       ├── JwtService.java
│       └── filters/
│           └── AuthenticationFilter.java
│
├── 📂 user-service/                         # Auth + User Management
│   └── src/main/java/.../
│       ├── controller/AuthController.java
│       ├── service/AuthService.java
│       ├── service/JwtService.java
│       ├── entity/User.java
│       ├── repository/UserRepository.java
│       └── utils/PasswordUtil.java
│
├── 📂 posts-service/                        # Posts + Likes
│   └── src/main/java/.../
│       ├── controller/PostsController.java
│       ├── controller/LikesController.java
│       ├── service/PostsService.java
│       ├── service/PostLikeService.java
│       ├── entity/Post.java
│       ├── entity/PostLike.java
│       ├── clients/ConnectionsClient.java
│       ├── event/PostCreatedEvent.java
│       └── event/PostLikedEvent.java
│
├── 📂 connections-service/                  # Social Graph (Neo4j)
│   └── src/main/java/.../
│       ├── controller/ConnectionsController.java
│       ├── service/ConnectionsService.java
│       ├── entity/Person.java
│       ├── repository/PersonRepository.java
│       ├── event/SendConnectionRequestEvent.java
│       └── event/AcceptConnectionRequestEvent.java
│
├── 📂 notification-service/                 # Event-driven Notifications
│   └── src/main/java/.../
│       ├── consumer/PostsServiceConsumer.java
│       ├── consumer/ConnectionsServiceConsumer.java
│       ├── service/SendNotification.java
│       ├── entity/Notification.java
│       └── clients/ConnectionsClient.java
│
└── 📂 uploader-service/                     # Cloudinary File Upload
    └── src/main/java/.../
        ├── controller/FileUploadController.java
        └── service/CloudinaryFileUploaderService.java
```

---

## ⚙️ Configuration

### Environment Variables

| Variable | Service | Description |
|---|---|---|
| `USER_SERVICE_URL` | api-gateway | Override USER-SERVICE URL (K8s mode) |
| `POSTS_SERVICE_URL` | api-gateway | Override POSTS-SERVICE URL (K8s mode) |
| `CONNECTIONS_SERVICE_URL` | api-gateway | Override CONNECTIONS-SERVICE URL (K8s mode) |
| `NEO4J_DB_URL` | connections-service | Neo4j Bolt URI |
| `NEO4J_DB_USERNAME` | connections-service | Neo4j username |
| `NEO4J_DB_PASSWORD` | connections-service | Neo4j password |
| `CLOUDINARY_CLOUD_NAME` | uploader-service | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | uploader-service | Cloudinary API key |
| `CLOUDINARY_SECRET_KEY` | uploader-service | Cloudinary secret |
| `SPRING_PROFILES_ACTIVE` | all (K8s) | Set to `k8s` in Kubernetes |

### Spring Profiles

| Profile | Mode | Discovery | Kafka | DB URLs |
|---|---|---|---|---|
| `default` | Local / Docker | Eureka `lb://` | `kafka:9092` | Service names |
| `k8s` | Kubernetes | K8s DNS `http://` | `kafka:9092` | K8s service names |

---

## 🤝 Design Patterns Used

| Pattern | Where Used | Purpose |
|---|---|---|
| **API Gateway** | `api-gateway` | Single entry point, auth, routing |
| **Service Discovery** | Eureka + `lb://` | Load-balanced service resolution |
| **Event-Driven** | Kafka topics | Decoupled async notification delivery |
| **Fan-out** | notification-service | Broadcast post events to all connections |
| **User Context Propagation** | All services | Pass userId via `X-User-Id` header + ThreadLocal |
| **Feign Interceptor** | posts-service | Auto-propagate headers on REST calls |
| **Repository Pattern** | All services | Clean separation of data access logic |
| **DTO Pattern** | All services | Prevent entity leakage from API responses |
| **Graph Database** | Neo4j | Optimal for social connection traversal |
| **Multi-profile Config** | All services | Single codebase for Docker + K8s |

---

## 📄 Docker Images

All images are published to Docker Hub under `nouman886`:

| Image | Tag |
|---|---|
| `nouman886/linkedin-app-discovery-server` | `latest` |
| `nouman886/linkedin-app-api-gateway` | `latest` |
| `nouman886/linkedin-app-user-service` | `latest` |
| `nouman886/linkedin-app-posts-service` | `latest` |
| `nouman886/linkedin-app-connections-service` | `latest` |
| `nouman886/linkedin-app-notification-service` | `latest` |

---

<div align="center">

**Built with ❤️ using Spring Boot Microservices**

Java 21 · Spring Boot 4 · Apache Kafka · Neo4j · PostgreSQL · Docker · Kubernetes

</div>

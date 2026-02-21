# 👔 LinkedIn Microservices Clone

A comprehensive, production-ready LinkedIn clone built using **Spring Boot Microservices architecture**. This project demonstrates best practices in distributed systems, including service discovery, API gateway patterns, asynchronous communication with Kafka, and heterogeneous database management (SQL & Graph).

---

## 🏗️ Architecture Overview

The system follows a microservices architectural pattern where each service is independent, scalable, and communicates over REST or messaging queues.

- **Frontend Gateway**: [Spring Cloud Gateway](./api-gateway)
- **Service Discovery**: [Netflix Eureka](./discovery-server)
- **Message Broker**: [Apache Kafka](./docker-compose.yml)
- **Identity & Profiles**: [User Service](./user-service)
- **Social Graph**: [Connections Service](./connections-service)
- **Feeds & Content**: [Posts Service](./posts-service)
- **Real-time Alerts**: [Notification Service](./notification-service)
- **Media Management**: [Uploader Service](./uploader-service)

---

## 🛠️ Technology Stack

| Category | Technology |
| :--- | :--- |
| **Framework** | Spring Boot 3.x, Spring Cloud |
| **Databases** | PostgreSQL (Relational), Neo4j (Graph), Cloudinary (Media) |
| **Messaging** | Apache Kafka |
| **Discovery** | Netflix Eureka |
| **Security/Routing** | Spring Cloud Gateway |
| **DevOps** | Docker, Docker Compose, Kubernetes |
| **Build Tool** | Maven |

---

## 📦 Service Detailed Breakdown

### 1. `api-gateway`
The entry point for all client requests. It handles routing to appropriate microservices and can be extended for authentication, rate limiting, and logging.
- **Port**: `8080`

### 2. `discovery-server`
Powered by Netflix Eureka, this service allows all microservices to register themselves and discover other services dynamically without hardcoded IP addresses.
- **Port**: `8761`

### 3. `user-service`
Handles user registration, authentication, and profile management.
- **Database**: PostgreSQL (`userDB`)
- **Key Features**: Profile CRUD, User Details.

### 4. `posts-service`
Manages the creation, retrieval, and interaction with posts (likes, comments).
- **Database**: PostgreSQL (`postsDB`)
- **Key Features**: Posting content, Feed generation.

### 5. `connections-service`
Manages the complex professional network. Uses a graph database to efficiently handle "friend of friend" queries and connection requests.
- **Database**: Neo4j (`connections-db`)
- **Key Features**: Connection requests, network growth, graph traversal.

### 6. `notification-service`
Listens to events from Kafka (like new connections or post likes) and sends notifications to users.
- **Database**: PostgreSQL (`notificationDB`)
- **Messaging**: Kafka Consumer.

### 7. `uploader-service`
Handles media uploads (images/videos) to the cloud.
- **Provider**: Cloudinary.

---

## 📂 Project Structure

```text
.
├── api-gateway            # Spring Cloud Gateway
├── user-service           # User & Profile Management
├── posts-service          # Post & Feed Management
├── connections-service    # Social Graph (Neo4j)
├── notification-service   # Event-driven Notifications
├── uploader-service       # Media Uploads (Cloudinary)
├── discovery-server       # Service Registration (Eureka)
├── k8s/                   # Kubernetes Deployment Manifests
├── docker-compose.yml     # Infrastructure (DBs, Kafka, Services)
└── README.md              # Documentation
```

---

## 🚀 Getting Started

### 1. Clone the Repository
First, clone the project to your local machine:
```bash
git clone https://github.com/Adnan-Umar/linkedin-app.git
cd linkedin-app
```

### 2. Prerequisites
- Java 17 or higher
- Maven 3.x
- Docker & Docker Compose

### 3. Build and Package
To build all services locally:
```bash
# In each service directory
mvn clean install
```
Alternatively, you can build Docker images for each service using the provided `Dockerfile` in each folder.

### 4. Running with Docker Compose
To spin up the entire infrastructure (Databases, Kafka, and Services) at once:

```bash
docker-compose up -d
```

### Accessing the Dashboard
- **Eureka Dashboard**: `http://localhost:8761`
- **API Gateway**: `http://localhost:8080`
- **Kafka UI**: `http://localhost:8090`
- **Neo4j Browser**: `http://localhost:7474`

---

## ☸️ Kubernetes Deployment

The project includes production-grade Kubernetes manifests in the `/k8s` directory. Each service has its own deployment and service configuration.

To deploy to a cluster:
```bash
kubectl apply -f k8s/
```

---

## 📬 Communication Flow
1. **Synchronous**: Services interact via REST when immediate data is needed.
2. **Asynchronous**: Events (e.g., `POST_CREATED`, `CONNECTION_REQUESTED`) are published to **Kafka**, and the `notification-service` consumes them to alert users.

---

Created by [Adnan Umar](https://github.com/Adnan-Umar) 🚀

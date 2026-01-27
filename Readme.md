# Hexamarket - High Performance E-Commerce Backend

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.9-green) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue) ![Redis](https://img.shields.io/badge/Redis-7-red) ![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-orange)

**Hexamarket** is a robust, modular monolithic backend system designed for B2C e-commerce platforms. It serves as a Resource Server within a Client-Side Rendering (CSR) architecture, prioritizing data integrity, high concurrency handling, and security.

This project demonstrates advanced techniques in Spring Boot, including **Optimistic Locking** for inventory, **JSONB** for dynamic product attributes, and **Stateless JWT Authentication**.
## 🏗 Architecture

![Hexamarket Architecture Diagram](architecture-diagram.png)
---

## 🚀 Key Features

### 1. Authentication & Authorization (Security)
* **Stateless Architecture:** Uses Spring Security 6 with JWT (Access Token & Refresh Token).
* **Token Rotation:** Secure implementation of Refresh Tokens.
* **Logout Mechanism:** Redis-based **Token Blacklist** for immediate token invalidation.
* **RBAC:** Role-Based Access Control (Admin, User, etc.).

### 2. Product Catalog (EAV & JSONB)
* **Dynamic Attributes:** Uses PostgreSQL **JSONB** and GIN Indexing to store and search dynamic product specs (e.g., RAM, Screen Size) without altering the schema.
* **Category Tree:** Nested categorization support.
* **Soft Delete:** Data preservation using `is_deleted` flags and JPA Specifications.

### 3. Order & Inventory (Concurrency Control)
* **Atomicity:** strict `@Transactional` boundaries for order placement.
* **Overselling Protection:** Implements **Optimistic Locking** (`@Version` column) to handle concurrent inventory updates.
* **Order State Machine:** Strict lifecycle management (PENDING -> CONFIRMED -> SHIPPING -> COMPLETED).
* **Auto-Cancellation:** Redis TTL or Scheduled Jobs to cancel unpaid orders after 30 minutes.

### 4. Performance & Scalability
* **Caching:** Redis caching for Product details and User Sessions.
* **Async Processing:** RabbitMQ integration for decoupling non-critical tasks (Email notifications, Audit logs).
* **Database Optimization:** Strategic indexing (Partial Indexes, Composite Indexes) defined in Flyway migrations.
* **Rate Limiting:** Redis-based rate limiting to prevent DDOS/Spam on sensitive APIs (Login).

---

## 🛠 Tech Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Java 21 | Latest LTS version |
| **Framework** | Spring Boot 3.5.9 | Core framework |
| **Database** | PostgreSQL 16 | Primary relational database (Dockerized) |
| **Caching** | Redis 7 | Caching & Token Blacklist |
| **Message Queue** | RabbitMQ | Async event handling |
| **Migration** | Flyway | Database version control |
| **ORM** | Spring Data JPA | Hibernate implementation |
| **Mapper** | MapStruct | High-performance DTO mapping |
| **Docs** | OpenAPI / Swagger | API Documentation |
| **Container** | Docker Compose | Orchestration for DB, Redis, and MQ |

---

## ⚙️ Prerequisites

* **Java 21** Development Kit (JDK)
* **Maven** 3.8+
* **Docker** & **Docker Compose**

---

## 📦 Installation & Setup

### 1. Clone the repository
```bash
git clone https://github.com/trantuanhao123/SpringBoot_HexaMarket.git
cd code
```
### 2. Infrastructure Setup (Docker)
Start the required services (PostgreSQL, Redis, RabbitMQ, RedisInsight) using Docker Compose:
```bash
docker-compose up -d
```
* **Postgres**: Port 5432
* **Redis**: Port 6379
* **RabbitMQ**: Port 5672 (Console: 15672)
* **RedisInsight**: Port 5540
### 3. Environment Configuration
Create a `.env` file in the root directory. Below is a ready-to-use configuration for local development (compatible with the provided Docker Compose):

```properties
# --- SERVER CONFIG ---
SERVER_PORT=8080
SPRING_PROFILE=local

# --- DATABASE (Connect to Docker container from Host) ---
DB_URL=jdbc:postgresql://localhost:5432/hexamarket
DB_USERNAME=hexamarket
DB_PASSWORD=hexamarket123
DB_DRIVER=org.postgresql.Driver
DB_POOL_MAX=20
DB_POOL_MIN=5
DB_TIMEOUT=30000

# --- REDIS ---
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_TTL=600000

# --- RABBITMQ ---
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=hexamarket
RABBITMQ_PASSWORD=hexamarket123

# --- JWT & PAYMENT SECURITY ---
# (For local dev, these specific keys are fine. Change them for production)
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_ACCESS_EXP=900000      # 15 minutes
JWT_REFRESH_EXP=604800000  # 7 days
PAYMENT_SECRET=localsecretkey

# --- RATE LIMIT ---
RATELIMIT_REQUESTS=600
RATELIMIT_LOGIN=10

# --- SECURITY DEFAULT ---
SECURITY_USER=admin
SECURITY_PASS=admin123

# --- EMAIL (Optional for local run if feature disabled) ---
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
### 4. Build and Run
The application uses Flyway, so database tables will be created automatically upon the first run.
```bash
mvn clean install
mvn spring-boot:run
```
### 📖 API Documentation
Once the application is running, you can access the Swagger UI to explore and test the APIs:

* **URL**: http://localhost:8080/swagger-ui.html

* **Docs**: http://localhost:8080/v3/api-docs

### 🗄 Database Schema Highlights
The system uses V1__init__schema.sql for the initial structure. Key design decisions include:

JSONB for Variants: product_variants table uses a JSONB column attributes for flexible product specs.

GIN Indexing: CREATE INDEX idx_variants_attributes_gin ensures fast searching within JSON data.

Partial Indexing: CREATE INDEX idx_user_address_default ensures extremely fast lookup for a user's default shipping address.

Optimistic Locking: inventory table includes a version column to prevent race conditions during checkout.

### 📂 Project Structure
The project follows a Package by Feature (or hybrid) approach for better modularity:
```text
src/main/java/com/hexamarket/code
├── component       # Event listeners (e.g., OrderTimeout)
├── config          # Security, Redis, RabbitMQ, OpenAPI configs
├── constant        # Enums (OrderStatus, etc.)
├── controller      # REST Controllers
├── dto             # Request/Response records
├── entity          # JPA Entities
├── exception       # Global Exception Handling
├── mapper          # MapStruct Interfaces
├── repository      # JPA Repositories
├── service         # Business Logic
└── util            # Security & Helper Utils
```
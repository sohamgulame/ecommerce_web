# 🛒 Full-Stack Enterprise E-Commerce Platform

A production-grade, full-stack E-Commerce web application built with **Spring Boot 3.5**, **Java 17**, **React (Vite)**, **MySQL 8**, **Redis**, **Docker Compose**, and **Tailwind CSS**.

---

## 🏗️ Architecture Overview

```
+-------------------------------------------------------------------------------+
|                             CLIENT / BROWSER                                  |
|               React SPA (Vite + Tailwind CSS + React Router)                  |
+-------------------------------------------------------------------------------+
                                      │ HTTP / JSON
                                      ▼
+-------------------------------------------------------------------------------+
|                          DOCKER / REVERSE PROXY                               |
|                          Nginx Alpine (Port 5173 / 80)                        |
+-------------------------------------------------------------------------------+
                                      │ Proxy to API
                                      ▼
+-------------------------------------------------------------------------------+
|                       SPRING BOOT 3 REST BACKEND (Port 8080)                  |
|                                                                               |
|  [Security & Rate Limiter] ──> [JWT & Cookie Filter] ──> [Controllers]       |
|                                                                 │             |
|  [Global Exception Handler] <── [DTO / Mapper Layer] <──────────┘             |
|                                         │                                     |
|  [Async Email Executor] <── [Service Layer (Spring Cache)]                    |
|  [Scheduled Auto-Cancel]                │                                     |
|                                         ▼                                     |
|                             [Spring Data JPA Repositories]                    |
+-------------------------------------------------------------------------------+
              │                                      │                   │
              ▼                                      ▼                   ▼
    +-------------------+                  +-------------------+  +---------------+
    |   MySQL 8.0 DB    |                  |    Redis Cache    |  |  Cloudinary   |
    | (Flyway Migrated) |                  | (JSON Serializer) |  | Media Storage |
    +-------------------+                  +-------------------+  +---------------+
```

---

## 🚀 Key Features

* **Authentication & Security**:
  * JWT Stateless Authentication with access tokens & refresh token rotation.
  * **HttpOnly, SameSite=Strict Cookies** for refresh tokens (eliminates XSS token theft).
  * **IP-based Sliding Window Rate Limiting** on auth endpoints against brute-force attacks.
  * **Email OTP Verification** on registration and password reset.
  * Role-based access control (`ROLE_CUSTOMER`, `ROLE_ADMIN`).
* **Product & Category Catalog**:
  * Pagination, category filtering, keyword search, price range filtering, and in-stock toggles.
  * High-performance **Redis Caching** (`@Cacheable`) with automatic `@CacheEvict` and silent MySQL fallback (`CacheErrorHandler`).
  * Cloudinary cloud image upload with multi-stage retry resilience.
* **Shopping Cart & Checkout**:
  * Database-backed cart scoped to authenticated user principal.
  * Concurrency-safe atomic inventory decrements (`decreaseStockIfAvailable`).
  * Instant checkout converting cart items to immutable order snapshots.
* **Order Management & State Machine**:
  * Strict status lifecycle: `PLACED` ➔ `CONFIRMED` ➔ `SHIPPED` ➔ `DELIVERED` (or `CANCELLED`).
  * **Automated Background Job (`@Scheduled`)**: Cancels unpaid/stale `PLACED` orders older than 24h and restores reserved inventory.
  * **Non-Blocking Email Notifications (`@Async`)**: Order confirmation and status update emails.
* **Customer Reviews & Ratings**:
  * One-review-per-user-per-product database constraint.
  * Dynamic average rating calculation.
* **DevOps & Production Readiness**:
  * **Flyway version-controlled database migrations** (`V1__init_schema.sql`).
  * **Spring Boot Actuator** health probes (`/actuator/health`, `/actuator/metrics`).
  * **HikariCP connection pool hardening** with connection leak detection.
  * **Multi-stage Dockerfiles** with non-root security and **Docker Compose** orchestration.

---

## ⚡ Quick Start with Docker (Recommended)

Run the entire full-stack application (MySQL, Redis, Backend, and Frontend) with a single command:

```powershell
# 1. Clone repository
git clone <repository-url>
cd project

# 2. (Optional) Configure environment
Copy-Item .env.example .env

# 3. Launch all containers
docker compose up --build -d
```

### Accessing Services:
* **Frontend Web App**: [http://localhost:5173](http://localhost:5173)
* **Backend REST API**: [http://localhost:8080/api/v1/health](http://localhost:8080/api/v1/health)
* **Actuator Health & Metrics**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
* **Stop Containers**: `docker compose down`

---

## 💻 Local Development Setup (Without Docker)

### Prerequisites:
* **Java 17+** (JDK)
* **Node.js 20+** & npm
* **MySQL 8.0** running on `localhost:3306` with database `projectdb`
* **Redis** running on `localhost:6379` (optional; falls back to DB)

### 1. Start Backend:
```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

### 2. Start Frontend:
```powershell
cd frontend
npm install
npm run dev
```

---

## 🔑 Default Seeded Accounts

| Role | Email | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@example.com` | `password123` | Full CRUD on products, categories, orders, image uploads |
| **Customer** | `customer1@example.com` | `password123` | Browse catalog, cart, checkout, view order history, submit reviews |

---

## 📖 REST API Reference

### Authentication (`/api/v1/auth`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/v1/auth/register` | Public | Register customer account & send verification OTP |
| `POST` | `/api/v1/auth/verify-email` | Public | Verify email with 6-digit OTP |
| `POST` | `/api/v1/auth/resend-otp` | Public | Resend verification OTP |
| `POST` | `/api/v1/auth/login` | Public | Authenticate user; returns JWT & sets HttpOnly cookie |
| `POST` | `/api/v1/auth/refresh` | Public | Rotate refresh token and issue new JWT access token |
| `POST` | `/api/v1/auth/logout` | Public | Invalidate refresh token and clear cookie |
| `POST` | `/api/v1/auth/forgot-password` | Public | Request password reset OTP |
| `POST` | `/api/v1/auth/reset-password` | Public | Reset password using OTP |

### Products & Categories (`/api/v1/products`, `/api/v1/categories`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/v1/categories` | Public | List categories (paginated, Redis cached) |
| `POST` | `/api/v1/categories` | Admin | Create new category (evicts cache) |
| `PUT` | `/api/v1/categories/{id}` | Admin | Update category (evicts cache) |
| `DELETE` | `/api/v1/categories/{id}` | Admin | Delete category (evicts cache) |
| `GET` | `/api/v1/products` | Public | Search/filter product catalog (paginated, cached) |
| `GET` | `/api/v1/products/{id}` | Public | Get product details with reviews (cached) |
| `POST` | `/api/v1/products` | Admin | Create new product (evicts cache) |
| `PUT` | `/api/v1/products/{id}` | Admin | Update product (evicts cache) |
| `DELETE` | `/api/v1/products/{id}` | Admin | Delete product (evicts cache) |
| `POST` | `/api/v1/products/{id}/images` | Admin | Upload product image to Cloudinary |
| `DELETE` | `/api/v1/products/{id}/images` | Admin | Delete product image |

### Cart & Orders (`/api/v1/cart`, `/api/v1/orders`, `/api/v1/admin/orders`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/v1/cart` | Customer | Get current authenticated user's cart |
| `POST` | `/api/v1/cart/items` | Customer | Add item to cart with stock validation |
| `PUT` | `/api/v1/cart/items/{itemId}` | Customer | Update item quantity in cart |
| `DELETE` | `/api/v1/cart/items/{itemId}` | Customer | Remove item from cart |
| `POST` | `/api/v1/orders` | Customer | Checkout: convert cart to order (201 Created) |
| `GET` | `/api/v1/orders` | Customer | List my orders (paginated) |
| `GET` | `/api/v1/orders/{id}` | Customer | Get order details (ownership validated) |
| `GET` | `/api/v1/admin/orders` | Admin | List all orders with optional status filter |
| `PUT` | `/api/v1/admin/orders/{id}/status` | Admin | Transition order status in state machine |

### Reviews (`/api/v1/reviews`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/v1/reviews` | Customer | Submit verified review & rating (201 Created) |
| `GET` | `/api/v1/products/{productId}/reviews` | Public | List reviews for product (paginated) |

---

## ⚙️ Environment Variables Reference

| Variable | Default | Description |
| :--- | :--- | :--- |
| `DB_NAME` | `projectdb` | MySQL database name |
| `DB_USERNAME` | `root` | MySQL database user |
| `DB_PASSWORD` | *(Required)* | MySQL database password |
| `JWT_SECRET` | *(Required)* | 256-bit HMAC secret key |
| `JWT_EXPIRATION_MS` | `3600000` (1h) | Access token lifetime |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` (7d) | Refresh token lifetime |
| `REDIS_HOST` | `redis` | Redis server hostname |
| `REDIS_PORT` | `6379` | Redis server port |
| `CLOUDINARY_CLOUD_NAME` | *(Optional)* | Cloudinary cloud account name |
| `CLOUDINARY_API_KEY` | *(Optional)* | Cloudinary API Key |
| `CLOUDINARY_API_SECRET` | *(Optional)* | Cloudinary API Secret |
| `SPRING_MAIL_HOST` | `smtp.gmail.com` | SMTP host |
| `SPRING_MAIL_PORT` | `587` | SMTP port |
| `SPRING_MAIL_USERNAME` | *(Optional)* | SMTP sender email |
| `SPRING_MAIL_PASSWORD` | *(Optional)* | SMTP App Password |

---

## 🧪 Testing

Run the full automated test suite:

```powershell
.\mvnw.cmd test
```

* **47 passing automated unit and integration tests** (0 failures, 0 errors).
* Includes MockMvc controller tests, JPA repository tests, JWT security filter tests, atomic stock checkout integration tests, and Redis cache integration tests.

---

## 📜 License

This project is licensed under the MIT License.

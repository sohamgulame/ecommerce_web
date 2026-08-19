# Project Summary — Full-Stack E-commerce Application

This summary provides a complete, high-level onboarding guide for developers and AI agents to quickly understand the architecture, run both backend and frontend applications, and contribute safely to the codebase.

---

## 📌 Quick Facts & Tech Stack

- **Backend Framework**: Java 17, Spring Boot 3.x (Maven Wrapper: `mvnw.cmd` / `mvnw`)
- **Backend Architecture**: Layered (Controller → Service → Repository → Entity) with DTOs, Mappers, and centralized Exception Handling
- **Database (Dev)**: MySQL 8.x (Local instance `projectdb`), H2 in-memory (Test profile)
- **Authentication & Security**: Spring Security 6, JWT Access Tokens (1h), Database-backed Refresh Token Rotation (7d), Scheduled Token Cleanup, CORS Origin Configuration
- **File & Media Storage**: Cloudinary Java SDK (Cloud CDN delivery, MIME & 5MB file validation, UI fallback containers)
- **Email & Notifications**: Spring Boot Mail (`JavaMailSender`), HTML email templates for OTP verification, password reset, and order notifications with resilient logging fallback
- **Testing**: JUnit 5, Mockito, MockMvc, H2 Integration Tests (46 passing automated tests), IntelliJ HTTP Client ([api-tests.http](file:///d:/CODES/SPRING%20BOOT/project/project/api-tests.http))
- **Current Completion Status**: Completed through **Phase 7** (Product Images & Cloudinary), **Phase 8** (Testing Suite), **Phase 8.5** (React Frontend & Admin Dashboard), and **Phase 9** (Email Service, OTP Verification & Order Notifications)

---

## 🚀 How to Build & Run

### Prerequisites
1. **Java 17+** (JDK)
2. **Node.js (v18+)** and `npm`
3. **MySQL Server** running locally on port 3306 with database `projectdb` created (`CREATE DATABASE IF NOT EXISTS projectdb;`)

### 1. Run the Spring Boot Backend
From the project root:
```powershell
# Run the test suite (46 tests)
.\mvnw.cmd test

# Build package skipping tests
.\mvnw.cmd -DskipTests package

# Start the Spring Boot server (port 8080)
.\mvnw.cmd spring-boot:run
```
*Alternatively, run `ProjectApplication.java` directly from your IDE.*

### 2. Run the React Frontend
From the `frontend` directory:
```powershell
cd frontend
npm install
npm run dev
```
The frontend starts by default on **`http://localhost:5173`**.

### 3. Health Check
Once the backend boots, verify server health:
- `GET http://localhost:8080/api/v1/health` → `{"status": "UP", "timestamp": "..."}`

---

## ⚙️ Configuration & Environment Variables

Backend Configuration File: [application.properties](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/resources/application.properties)

| Property Name | Default / Fallback | Environment Variable | Purpose |
| :--- | :--- | :--- | :--- |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/projectdb` | — | Local MySQL JDBC URL |
| `spring.datasource.username` | `root` | `DB_USERNAME` | Database username |
| `spring.datasource.password` | `(required)` | `DB_PASSWORD` | Database password |
| `jwt.secret` | `(required)` | `JWT_SECRET` | Secret key for signing HMAC-SHA256 JWTs |
| `jwt.expiration-ms` | `3600000` (1 hr) | `JWT_EXPIRATION_MS` | Access token lifespan |
| `jwt.refresh-expiration-ms` | `604800000` (7 days) | `JWT_REFRESH_EXPIRATION_MS` | Refresh token lifespan |
| `cors.allowed-origins` | `http://localhost:*,http://127.0.0.1:*` | `CORS_ALLOWED_ORIGINS` | Permitted frontend origins |
| `cloudinary.cloud-name` | `(optional)` | `CLOUDINARY_CLOUD_NAME` | Cloudinary account cloud name |
| `cloudinary.api-key` | `(optional)` | `CLOUDINARY_API_KEY` | Cloudinary API Key |
| `cloudinary.api-secret` | `(optional)` | `CLOUDINARY_API_SECRET` | Cloudinary API Secret |
| `spring.servlet.multipart.max-file-size` | `5MB` | — | Maximum single image upload size |
| `spring.servlet.multipart.max-request-size` | `10MB` | — | Maximum multipart request size |

> [!TIP]
> **MySQL Public Key Retrieval**: If you encounter `Public Key Retrieval is not allowed` on MySQL 8, ensure your JDBC URL includes `?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC`.

---

## 📡 API Overview (Key Endpoints)

Base URL: `http://localhost:8080/api/v1`

### Authentication & Tokens (`/auth`)
- `POST /api/v1/auth/register` — Register a new user (`CUSTOMER` role by default) and dispatch 6-digit verification OTP. Returns `{ accessToken, refreshToken, user }`
- `POST /api/v1/auth/login` — Authenticate credentials. Returns `{ accessToken, refreshToken, user }`
- `POST /api/v1/auth/verify-email` — Verify email using 6-digit OTP code and mark account verified
- `POST /api/v1/auth/resend-otp` — Resend verification or password reset OTP to user's registered email
- `POST /api/v1/auth/forgot-password` — Request password reset OTP email
- `POST /api/v1/auth/reset-password` — Set new password using verified 6-digit OTP
- `POST /api/v1/auth/refresh` — Rotate single-use refresh token and receive a fresh access token pair
- `POST /api/v1/auth/logout` — Server-side revocation of the active refresh token

### Categories (`/categories`)
- `GET /api/v1/categories` — List all categories (Public)
- `GET /api/v1/categories/{id}` — Get category by ID (Public)
- `POST /api/v1/categories` — Create category (`ADMIN` only)
- `PUT /api/v1/categories/{id}` — Update category (`ADMIN` only)
- `DELETE /api/v1/categories/{id}` — Delete category (`ADMIN` only)

### Products (`/products`)
- `GET /api/v1/products` — Paginated list with search & filters (`page`, `size`, `sort`, `categoryId`, `search`, `minPrice`, `maxPrice`) (Public)
- `GET /api/v1/products/{id}` — Product details with category name, image list, and live average rating (Public)
- `POST /api/v1/products` — Create product (`ADMIN` only)
- `PUT /api/v1/products/{id}` — Update product details and stock (`ADMIN` only)
- `DELETE /api/v1/products/{id}` — Delete product (`ADMIN` only)
- `POST /api/v1/products/{id}/images` — Upload multipart product image to Cloudinary and append URL (`ADMIN` only)
- `DELETE /api/v1/products/{id}/images` — Remove image URL from product (`ADMIN` only)

### Cart (`/cart`)
*Resolved dynamically from authenticated user principal (JWT)*
- `GET /api/v1/cart` — View current user's cart with itemized subtotals and computed total
- `POST /api/v1/cart/items` — Add product to cart (Validates against available stock)
- `PUT /api/v1/cart/items/{id}` — Update item quantity (Prevents exceeding stock)
- `DELETE /api/v1/cart/items/{id}` — Remove single cart item
- `DELETE /api/v1/cart` — Clear entire cart

### Orders & Checkout (`/orders` & `/admin/orders`)
- `POST /api/v1/orders` — Checkout: converts active cart into order, snapshots item names/prices, decrements stock atomically, and clears cart
- `GET /api/v1/orders` — Paginated order history for current customer
- `GET /api/v1/orders/{id}` — View order details (IDOR-protected: owner or `ADMIN` only)
- `GET /api/v1/admin/orders` — Paginated list of all customer orders, filterable by `OrderStatus` (`ADMIN` only)
- `PUT /api/v1/admin/orders/{id}/status` — Update order status enforcing the state machine transition rules (`ADMIN` only)

### Reviews & Ratings (`/reviews`)
- `POST /api/v1/reviews` — Submit review & rating (1–5 stars). Verifies customer has a confirmed/delivered order for the product; enforces 1 review per user per product
- `GET /api/v1/products/{id}/reviews` — Paginated list of reviews for a product (Public)

### Media Upload (`/files`)
- `POST /api/v1/files/upload` — Direct multipart file upload to Cloudinary returning secure CDN URL (`ADMIN` only)

---

## 🏗️ Project Architecture & Key Packages

### Backend Structure (`src/main/java/com/Project1/project/`)
- [config/](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/config) — Spring configurations ([SecurityConfig.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/config/SecurityConfig.java), [JwtConfig.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/config/JwtConfig.java), [CloudinaryConfig.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/config/CloudinaryConfig.java), [DataInitializer.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/config/DataInitializer.java))
- [controller/](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/controller) — REST controllers (Thin controllers delegating all logic to services)
- [service/](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/service) & [impl/](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/service/impl) — Core business services, Cloudinary storage, token cleanup routines
- [repository/](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/repository) — Spring Data JPA repositories with custom JPQL queries and atomic update operations
- [entity/](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/entity) — JPA entities (`User`, `Role`, `Product`, `Category`, `Cart`, `CartItem`, `Order`, `OrderItem`, `OrderStatus`, `Review`, `RefreshToken`)
- [dto/](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/dto) & [mapper/](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/mapper) — Request/Response DTOs and entity mappers (decoupling API contract from database tables)
- [security/](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/security) — `JwtAuthenticationFilter`, `JwtUtil`, `CurrentUserProvider`
- [exception/](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/exception) — Mapped business domain exceptions and centralized [GlobalExceptionHandler.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/exception/GlobalExceptionHandler.java)

### Frontend Structure (`frontend/src/`)
- [api/axios.js](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/api/axios.js) — Axios instance with automatic JWT injection, 401 response interception, silent refresh rotation, and retry queue
- [context/AuthContext.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/context/AuthContext.jsx) — User session, role verification from JWT claims, login, logout, and token state
- [context/CartContext.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/context/CartContext.jsx) — Live client cart synchronization and badge counter
- [pages/](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages) — Customer pages ([ProductListPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/ProductListPage.jsx), [ProductDetailPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/ProductDetailPage.jsx), [CartPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/CartPage.jsx), [CheckoutPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/CheckoutPage.jsx), [OrderHistoryPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/OrderHistoryPage.jsx), [LoginPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/LoginPage.jsx), [RegisterPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/RegisterPage.jsx))
- [pages/admin/](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/admin) — Admin portal ([AdminProductsPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/admin/AdminProductsPage.jsx) with Cloudinary upload, [AdminCategoriesPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/admin/AdminCategoriesPage.jsx), [AdminOrdersPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/admin/AdminOrdersPage.jsx))

---

## 🛡️ Key Architectural Decisions & Concurrency Protections

1. **Atomic Stock Decrement & Concurrency Safety**:
   - Stock is validated upon adding to cart, re-validated upon updating cart quantity, and atomically decremented during checkout via a conditional database update:
     ```java
     @Modifying
     @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :id AND p.stockQuantity >= :quantity")
     int decreaseStockIfAvailable(@Param("id") Long id, @Param("quantity") Integer quantity);
     ```
   - If concurrent orders claim the last available stock, `decreaseStockIfAvailable` returns `0`, rolling back the transaction and raising an `InsufficientStockException` (HTTP 400).

2. **Historical Order Snapshots**:
   - `OrderItem` explicitly copies `productName` and `price` at checkout time into snapshot columns (`productNameSnapshot`, `priceSnapshot`). Future product name or price changes never alter historical receipts.

3. **Database Unique Constraints**:
   - `CartItem(cart_id, product_id)` — Prevents duplicate item rows under rapid concurrent clicks.
   - `Review(product_id, user_id)` — Guarantees at most one review per user per product.
   - `Category(name)` & `User(email)` — Unique constraints with corresponding `CategoryAlreadyExistsException` (409) and `EmailAlreadyExistsException` (409).

4. **Order Status State Machine**:
   - Valid transitions: `PLACED` → `CONFIRMED` → `SHIPPED` → `DELIVERED` | `PLACED`/`CONFIRMED` → `CANCELLED`.
   - Invalid jumps (e.g. `DELIVERED` → `PLACED`) throw `InvalidOrderStatusTransitionException` (HTTP 400).

5. **Self-Healing Data Initializer**:
   - [DataInitializer.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/config/DataInitializer.java) runs on non-test profiles (`@Profile("!test")`), automatically creates missing categories, seeds 15+ diverse multi-category products, and populates `product_images` with high-resolution imagery.

---

## 🧪 Testing & Verification

- **Automated Test Suite**: 46 tests across unit, MockMvc controller, and integration layers:
  - `OtpServiceImplTest` — OTP generation (6-digit numeric), SHA-256 hash matching, expiry checks, and single-use invalidation.
  - `EmailServiceImplTest` — Email template rendering, JavaMailSender dispatch, and fallback resilience when SMTP is unconfigured.
  - `AuthServiceImplTest` — Registration with email verification dispatch, OTP email verification, OTP resend cooldowns, forgot password, reset password, token rotation, and logout revocation.
  - `CartServiceImplTest` — Cart creation, item additions, quantity updates, stock boundary checks, cart clearing, and ownership enforcement.
  - `OrderServiceImplTest` & `OrderControllerTest` — State machine transitions, order confirmation/status update email notifications, IDOR verification, checkout snapshots.
  - `ReviewServiceImplTest` — Verified purchase checks, rating bounds, duplicate review prevention.
  - `CloudinaryStorageServiceImplTest` & `ProductControllerTest` — MIME/size validation and image upload handling.
  - `CheckoutIntegrationTest` — Full end-to-end H2 checkout lifecycle (Add to cart → Checkout → Stock decrement → Order creation → Cart clear).
- **HTTP Client Test Suite**: [api-tests.http](file:///d:/CODES/SPRING%20BOOT/project/project/api-tests.http) contains complete manual & automated test scenarios for IntelliJ IDEA HTTP Client / VS Code REST Client.

---

## 📂 Project Context Files (Agent & Developer Knowledge Base)

- [memory.md](file:///d:/CODES/SPRING%20BOOT/project/project/files/memory.md) — Chronological progress log, technical snapshots, and audit trail of agent actions.
- [error.md](file:///d:/CODES/SPRING%20BOOT/project/project/files/error.md) — Priority list of open production readiness gaps (P01–P10) and comprehensive resolution history (E01–E24).
- [design.md](file:///d:/CODES/SPRING%20BOOT/project/project/files/design.md) — Technical architecture design, ER models, DTO contracts, and frontend design rules.
- [phases.md](file:///d:/CODES/SPRING%20BOOT/project/project/files/phases.md) — Step-by-step development roadmap across all 13 phases.
- [prd.md](file:///d:/CODES/SPRING%20BOOT/project/project/files/prd.md) — Product Requirements Document outlining goals, user stories, and non-goals.
- [rules.md](file:///d:/CODES/SPRING%20BOOT/project/project/files/rules.md) — Coding conventions, architectural rules, and error handling standards.

---

## 🔮 Upcoming Phases & Production Roadmap

- **Phase 10 — Background Processing**: `@Scheduled` automated cancellation of unpaid orders, `@Async` non-blocking email dispatch.
- **Phase 11 — Performance & Caching**: Redis integration for high-traffic catalog read caching (`@Cacheable`) with `@CacheEvict` on updates.
- **Phase 12 — Docker & Deployment**: Multi-stage `Dockerfile`, `docker-compose.yml` (App + MySQL + Redis), cloud deployment with production profiles.
- **Phase 13 — Final Polish & Security Review**: Production Flyway database migrations, HttpOnly cookie refresh tokens, rate limiting (Bucket4j), and Actuator health probes.

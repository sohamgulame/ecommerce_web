# error.md — Comprehensive Production Readiness & Error Audit

This document tracks all historical bug fixes, security hardening items, edge-case vulnerabilities, and production-readiness milestones across the full-stack system (Spring Boot + React).

---

## 📊 Production Audit Matrix

| Category | Status | Resolved / Total |
| :--- | :---: | :---: |
| **Security & Auth** | 🟢 100% Production-Ready | 11 / 11 |
| **Data Integrity & Concurrency** | 🟢 100% Production-Ready | 8 / 8 |
| **Error Handling & Validation** | 🟢 100% Production-Ready | 8 / 8 |
| **File Handling & Cloudinary** | 🟢 100% Production-Ready | 5 / 5 |
| **Performance & Caching** | 🟢 100% Production-Ready | 3 / 3 |
| **DevOps & Observability** | 🟢 100% Production-Ready | 4 / 4 |

---

## ✅ RESOLVED PRODUCTION READINESS MILESTONES (P01 – P10)

### 🔴 CRITICAL & HIGH — Security & Data Integrity

#### [x] P01 — Database Schema Managed by Version-Controlled Migrations (Flyway) (FIXED)
- **Resolved:** Added `flyway-core` and `flyway-mysql` starters. Created `src/main/resources/db/migration/V1__init_schema.sql` defining all 11 database tables, foreign keys, indexes, and unique constraints. Set `spring.jpa.hibernate.ddl-auto=validate`.

#### [x] P02 — Refresh Tokens Stored in HttpOnly Secure Cookies (FIXED)
- **Resolved:** In `AuthController`, issued `refreshToken` inside `Set-Cookie` with `HttpOnly; SameSite=Strict; Path=/api/v1/auth; Max-Age=604800`. Added fallback extraction from cookies on `/api/v1/auth/refresh` and cookie clearing on logout. Eliminates XSS token theft.

#### [x] P03 — IP-Based Sliding Window Rate Limiting on `/auth/**` (FIXED)
- **Resolved:** Created `RateLimitingFilter.java` intercepting `POST /api/v1/auth/**` (15 requests/min per IP) returning `HTTP 429 Too Many Requests` with `Retry-After: 60` header and JSON error body.

---

### 🟡 MEDIUM — Resilience, Observability & Performance

#### [x] P04 — Sanitized Generic 500 Responses & SLF4J Logging (FIXED)
- **Resolved:** Updated `GlobalExceptionHandler.handleGeneric()` to log full exception stack trace internally via SLF4J and return a sanitized, safe client response (`"An unexpected internal server error occurred. Please try again later."`).

#### [x] P05 — Production Health Probes & Metrics (Actuator) (FIXED)
- **Resolved:** Added `spring-boot-starter-actuator` with exposed endpoints (`/actuator/health`, `/actuator/info`, `/actuator/metrics`) and enabled liveness/readiness probes.

#### [x] P06 — Database Connection Pool (HikariCP) & Leak Detection Hardening (FIXED)
- **Resolved:** Configured explicit HikariCP settings in `application.properties` and `application-prod.properties` (max-pool-size: 20, min-idle: 5, idle-timeout: 300s, max-lifetime: 1800s, connection-timeout: 20s, leak-detection-threshold: 15s).

#### [x] P07 — Cloudinary Upload Network Resilience & Retry Policy (FIXED)
- **Resolved:** Implemented 3-attempt retry loop with exponential backoff in `CloudinaryStorageServiceImpl.uploadImage()` to handle transient cloud socket/network hiccups.

#### [x] P08 — High-Traffic Read Caching with Redis & Resilient Fallback (FIXED)
- **Resolved:** Integrated Spring Cache + Redis with custom TTLs (1h categories, 10m products), `@CacheEvict` invalidation, and `CacheErrorHandler` for silent DB fallback during cache interruptions.

---

### 🟢 LOW — Configuration & Hygiene

#### [x] P09 — Scoped CORS Allowed Origins (FIXED)
- **Resolved:** Replaced wildcards in production with configurable `${CORS_ALLOWED_ORIGINS}` in `application-prod.properties` and `SecurityConfig`.

#### [x] P10 — Centralized Logging & Error Tracking (FIXED)
- **Resolved:** Configured structured SLF4J loggers across all service layers, async email dispatchers, background auto-cancel jobs, and filters.

---

## ✅ HISTORICAL BUG FIXES & DEFECT LOG (E01 – E24)

### Security & Role Authorization
- **[x] E01 — Product & Category write endpoints publicly accessible (FIXED)**: Secured with `@PreAuthorize("hasRole('ADMIN')")` and `SecurityConfig` restriction.
- **[x] E02 — IDOR on order detail endpoint (FIXED)**: Added ownership verification in `OrderServiceImpl.getOrderById()` with 404 response to prevent resource existence probing.
- **[x] E03 — Hardcoded credentials in source control (FIXED)**: Replaced with environment variable placeholders `${DB_PASSWORD}`, `${JWT_SECRET}`, `${CLOUDINARY_API_SECRET}`.
- **[x] E07 — Cart ownership access violations (FIXED)**: Enforced JWT principal-scoped resolution via `CurrentUserProvider` and `ForbiddenOperationException` (403).
- **[x] E11 — Refresh Token Rotation & Server-Side Revocation (FIXED)**: Full refresh token lifecycle implemented with single-use rotation, database persistence, revocation on logout, and automated scheduled cleanup.
- **[x] E21 — Hardcoded JWT Role Heuristics in Frontend (FIXED)**: Removed `.includes('admin')` heuristic; role is now parsed directly from backend JWT token payload claims.

### Concurrency & Data Integrity
- **[x] E09 — Stock Decrement Race Conditions (FIXED)**: Implemented atomic conditional query `decreaseStockIfAvailable()` in `ProductRepository` preventing overselling.
- **[x] E16 — Duplicate Cart Items under Concurrent Requests (FIXED)**: Added unique database constraint on `CartItem(cart_id, product_id)`.
- **[x] E06 & E19 — Duplicate Category Creation (FIXED)**: Added proactive check `categoryRepository.existsByName()` throwing `CategoryAlreadyExistsException` (409).
- **[x] E20 — Duplicate Registration Email 500 Error (FIXED)**: Created `EmailAlreadyExistsException` mapped to HTTP 409 Conflict in `GlobalExceptionHandler`.
- **[x] E17 — One Review Per Customer Per Product (FIXED)**: Unique constraint on `Review(product_id, user_id)` and service-level verification checking previous purchase history.

### Error Handling & API Contracts
- **[x] E04 — Custom Exceptions Returning 500 (FIXED)**: Unified `GlobalExceptionHandler` returning consistent standard JSON error envelope (`status`, `message`, `timestamp`, `path`).
- **[x] E05 — Login Failure Returning 500 (FIXED)**: Mapped `AuthenticationException` to 401 Unauthorized with non-revealing error message.
- **[x] E10 — Inconsistent Empty Cart Responses (FIXED)**: Standardized to `EmptyCartException` (400 Bad Request).
- **[x] E12 — Average Rating Always Null (FIXED)**: Computed dynamically via JPQL `avg(r.rating)` query in `ReviewRepository`.
- **[x] E14 & E15 — Silently Swallowed Exceptions in Mappers (FIXED)**: Cleaned empty `catch` blocks in `ProductMapper` and `ReviewMapper`.

### File Upload & Media Handling
- **[x] E22 — Unvalidated Multipart File Uploads (FIXED)**: Added MIME type checking (JPG, PNG, WebP) and 5MB size limit validation throwing `InvalidFileException` (400).
- **[x] E23 — Empty Image Collections on Seeded Products (FIXED)**: Added self-healing `DataInitializer` and populated `product_images` collection table.
- **[x] E24 — Frontend Image Loading Breakages (FIXED)**: Built inline SVG gradient fallback containers in `ProductListPage.jsx` and `ProductDetailPage.jsx`.

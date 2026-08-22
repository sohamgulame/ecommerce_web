# memory.md — Project Memory (Agent-Maintained)

This file is the running context log for this project. The agent should update it
after completing each phase, making a design decision, or hitting something worth
remembering for future sessions. Keep entries short and factual — this file exists so
a new chat session can pick up exactly where the last one left off.

---

## How to use this file (for the agent)
- Append, don't rewrite history — add new entries under the relevant section.
- After finishing a phase from `phases.md`, add a line under "Progress Log" with the
  date/phase and a one-line summary of what was built.
- If a design decision gets made that isn't already in `design.md`, record the
  decision here AND update the "Open Design Decisions" section of `design.md`.
- If the user changes a rule, scope item, or preference mid-project, log it here so it
  isn't lost or contradicted later.

---

## Project Snapshot
- **Project**: E-commerce full-stack app (Spring Boot backend + React frontend)
- **Learner's completed skills at project start**: REST APIs, JPA + relationships,
  clean layered architecture (DTO/Mapper/Exception handling), JWT auth + role-based
  authorization, pagination/search/filtering
- **Currently on phase**: Phase 13 — Polish & Review (All 13 Roadmap Phases & error.md Audits 100% Completed)
- **Tech stack locked in**:
  - Backend: Java, Spring Boot, Spring Data JPA, MySQL (local), Spring Security + JWT,
    Cloudinary (File Storage), Spring Mail, Maven
  - Frontend: React (Vite), Axios, React Router, Tailwind CSS

## Progress Log
*(add one line per completed phase, most recent at the top)*
- 2026-08-22: Cloud Deployment completed: Spring Boot Backend deployed live on Render Free Tier (`https://ecommerce-backend-81qt.onrender.com`), backed by Aiven Managed MySQL (`defaultdb`), in-memory ConcurrentMap caching (`SPRING_CACHE_TYPE=simple`), automatic Flyway repair and migrations with primary key compliance (`sql_require_primary_key=ON`), and verified live 200 OK responses on `/api/v1/health` and `/api/v1/categories`.
- 2026-08-20: Phase 13 — Polish & Review completed: resolved all production readiness gaps from `error.md` (P01: Flyway database migrations `V1__init_schema.sql` with `ddl-auto=validate`; P02: `HttpOnly; SameSite=Strict` refresh token cookies; P03: `RateLimitingFilter` sliding-window limiter on `/auth/**`; P04: sanitized 500 error messages with SLF4J stack logging; P05: Spring Boot Actuator health & metrics; P06: HikariCP connection pool hardening & leak detection threshold; P07: Cloudinary upload retry resilience with backoff; P08: Redis caching with graceful fallback; P09: Scoped production CORS); updated `ReviewController` to return 201 Created; created comprehensive production `README.md`. 47 passing tests. Full project 100% complete.
- 2026-08-19: Phase 12 — Docker & Deployment completed: built multi-stage backend `Dockerfile` (`eclipse-temurin:17` with layer caching & non-root user), frontend `Dockerfile` (`node:18` build + `nginx:alpine` SPA web server with `nginx.conf`), `application-prod.properties` profile with HikariCP tuning, `.env.example` template, and `docker-compose.yml` orchestrating MySQL 8, Redis Alpine, Spring Boot Backend, and React Frontend with healthchecks and persistent volumes. 47 passing tests.
- 2026-08-19: Phase 11 — Performance & Caching (Redis + Spring Cache) completed: integrated `spring-boot-starter-cache` and `spring-boot-starter-data-redis`, enabled `@EnableCaching`, built `RedisConfig` with Jackson polymorphic JSON serialization & custom TTLs (1h for categories, 10m for products), annotated `CategoryServiceImpl`, `ProductServiceImpl`, and `ReviewServiceImpl` with `@Cacheable` and `@CacheEvict`/`@Caching`, implemented `Serializable` on DTOs, and added `CacheIntegrationTest`. Full test suite expanded to 47 passing tests.
- 2026-08-19: Phase 10 — Background Processing completed: `@Scheduled` auto-cancellation of stale PLACED orders (configurable 24h threshold, every-30-min cron), stock restoration on cancel, cancellation notification email; `@Async` non-blocking email dispatch via dedicated `emailTaskExecutor` thread pool (2–5 threads). 46 passing tests.
- 2026-08-19: Phase 9 — Production Features (Email Service, OTP Verification, Password Reset & Order Notifications) completed: integrated Spring Boot Mail starter, EmailService with resilient HTML template rendering & fallback logging, OtpVerification entity & OtpService with SHA-256 hash storage & 15-minute expiration, User email verification on registration (`/auth/verify-email`, `/auth/resend-otp`), Forgot/Reset password flow (`/auth/forgot-password`, `/auth/reset-password`), order placement confirmation email & admin order status transition notification email, React frontend pages (`VerifyEmailPage.jsx`, `ForgotPasswordPage.jsx`, `ResetPasswordPage.jsx`), and full test suite expansion (46 passing tests).
- 2026-08-18: Phase 7 — Product Images (Cloudinary) completed: integrated Cloudinary SDK, FileStorageService with size/MIME validation, ProductController image endpoints (`POST/DELETE /{id}/images`), FileUploadController (`POST /files/upload`), and updated React AdminProductsPage with image drag-and-drop/upload previews. Full suite: 31 passing tests.
- 2026-08-17: Phase 8.5 — Frontend (React) completed: scaffolded Vite React app with Axios (JWT request interceptor), React Router, and Tailwind CSS. Implemented Product Catalog (search, category/price filters, Spring Page pagination), Product Detail (specs, reviews, review submission form), Cart & Checkout, Order History, Auth (Login/Register with JWT storage), and Admin Panel (Products CRUD, Categories CRUD, Order status state machine transitions).
- 2026-08-16: Phase 8.5 — Frontend (React) added to scope and fully specified in
  `phases.md`/`prd.md`/`design.md`.
- 2026-08-16: Phase 8 — Testing completed: cart, order transition, and review service
  unit tests; Product/Order MockMvc tests; H2-backed checkout integration test. Full
  suite: 22 passing tests.
- 2026-08-15: Phase 6 — Reviews & Ratings implemented (Review entity, repository,
  DTOs, mapper, service, controller, one-review-per-user-per-product constraint)
- 2026-08-15: Phase 5 — Order module implemented (Order, OrderItem entities,
  repositories, DTOs, service, controllers, order status state machine)
- 2026-08-15: Phase 3 — Authentication & Users implemented (User entity, Role enum,
  JWT auth, UserRepository, AuthController)
- 2026-08-15: Phase 2 — Product module implemented (Product entity, repository, DTOs,
  mapper, service, controller)
- 2026-08-15: Phase 4 — Cart module implemented (Cart, CartItem entities,
  repositories, DTOs, service, controller)
- 2026-08-15: Phase 1 — Category module implemented (entity, repository, DTOs,
  mapper, service, controller, global exception handler)

## Decisions Made
*(carry over confirmed answers from design.md's "Open Design Decisions" once settled)*
- **Database: MySQL, local instance** — not Supabase. Rationale: Spring Boot owns
  auth/business logic/file storage itself, so Supabase's BaaS features would go
  unused; local MySQL avoids dev/test latency and matches the planned Docker Compose
  setup in Phase 12; keeps consistency with the learner's existing demo project.
- **Frontend stack: React + Vite + Axios + React Router + Tailwind CSS** — chosen for
  fit with the existing JWT/REST architecture (SPA consuming a stateless API, not
  server-rendered), strongest placement/interview recognition ("Spring Boot + React"),
  and appropriate scope (no Next.js/Redux needed for this project's size). Full
  rationale and comparison against Thymeleaf/Angular/Vue/Next.js discussed and
  recorded in chat on 2026-08-16.
- **File Storage: Cloudinary** — chosen over AWS S3 / GCP Cloud Storage. Rationale:
  developer-friendly Java SDK, generous free tier, automatic image transformations/optimizations,
  global CDN delivery, and straightforward credential configuration (`cloud_name`, `api_key`, `api_secret`).

## Known Deviations from the Plan
*(anything built differently than `phases.md`/`design.md` originally specified, and
why)*
- 2026-08-16: Phase 7 (Product Images / File Handling) was deferred in favor of
  Testing (Phase 8) — validate existing modules before adding new surface area on top
  of unverified ones. Still pending, not abandoned — will resume after the frontend
  MVP or sooner if needed.
- 2026-08-16: Frontend scope added — **now formally resolved** in `prd.md` (Non-Goals
  updated, new §6a added), `phases.md` (new Phase 8.5 block with full sub-phase
  breakdown), and `design.md` (§7a Frontend Architecture + CORS requirement added to
  §7 Security Design). This was originally logged as a one-line footnote here before
  being properly specified — future deviations of this size should get the full
  treatment (PRD + phases + design) at the time they're decided, not just a memory
  note.

## Things to Revisit
*(shortcuts taken for now that should be cleaned up later, e.g. "used a simple enum
for payment status instead of proper Payment entity — revisit if scope grows")*
- Consider optimistic locking or stronger DB-level transaction strategy for
  high-concurrency stock updates (currently uses conditional update).
- Add database migrations (Flyway/Liquibase) instead of relying on
  hibernate.ddl-auto=update for production readiness.
- Add coverage for authentication and additional authorization edge cases as the API
  expands.
- **E11 (refresh token) — still open.** Only access tokens exist; no refresh flow. Not
  a bug, a planned-but-undone feature per `error.md`. Was dropped from this file
  before — re-adding here so it doesn't get lost again now that frontend work is
  starting (the frontend's auth flow will need to account for token expiry either way
  — short-lived access token + no refresh means users get logged out and have to
  re-login, which is fine for now but worth deciding on purpose, not by accident).
- **CORS is specified but not yet implemented.** `SecurityConfig` needs the frontend's
  dev origin allowed before any frontend request will actually reach the API — do
  this first, before building frontend pages that call the backend, or every request
  will fail with a CORS error that looks like a bug but isn't.

---

## Recent Work (agent actions)
*(most recent first — quick audit trail of fixes the agent applied)*
- 2026-08-20: Phase 13 — Polish, Production Readiness & Error Audit:
  - Added Flyway migration dependencies (`flyway-core`, `flyway-mysql`) and created [V1__init_schema.sql](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/resources/db/migration/V1__init_schema.sql) with full schema definition. Set `spring.jpa.hibernate.ddl-auto=validate`.
  - Hardened refresh token security with `HttpOnly; SameSite=Strict; Path=/api/v1/auth` cookies in [AuthController.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/controller/AuthController.java).
  - Created [RateLimitingFilter.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/security/RateLimitingFilter.java) (15 req/min per IP on sensitive auth endpoints with HTTP 429 Too Many Requests).
  - Sanitized generic 500 exception responses in [GlobalExceptionHandler.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/exception/GlobalExceptionHandler.java) and added internal SLF4J stack logging.
  - Added `spring-boot-starter-actuator` with exposed health and metrics endpoints.
  - Hardened HikariCP connection pool settings (max: 20, min: 5, leak detection: 15s) in `application.properties` and `application-prod.properties`.
  - Added exponential backoff retry loop to [CloudinaryStorageServiceImpl.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/service/impl/CloudinaryStorageServiceImpl.java).
  - Updated [ReviewController.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/controller/ReviewController.java) `POST /api/v1/reviews` to return `201 Created`.
  - Overhauled [README.md](file:///d:/CODES/SPRING%20BOOT/project/project/README.md) into a complete production guide.
  - Updated [error.md](file:///d:/CODES/SPRING%20BOOT/project/project/files/error.md) (all P01–P10 items resolved). 47/47 passing tests.
- 2026-08-19: Phase 12 — Docker & Deployment:
  - Created root [.dockerignore](file:///d:/CODES/SPRING%20BOOT/project/project/.dockerignore) and multi-stage backend [Dockerfile](file:///d:/CODES/SPRING%20BOOT/project/project/Dockerfile) using `eclipse-temurin:17-jdk-alpine` builder and minimal `eclipse-temurin:17-jre-alpine` runtime running as secure non-root `spring:spring` user.
  - Created [application-prod.properties](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/resources/application-prod.properties) with production database connection pool (HikariCP max=20, min=5), disabled SQL console logging, and production CORS origins.
  - Created [frontend/.dockerignore](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/.dockerignore), [frontend/nginx.conf](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/nginx.conf) (supporting HTML5 client SPA fallback routing, gzip compression, and security headers), and multi-stage [frontend/Dockerfile](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/Dockerfile) (`node:18-alpine` + `nginx:alpine`).
  - Created [docker-compose.yml](file:///d:/CODES/SPRING%20BOOT/project/project/docker-compose.yml) orchestrating 4 interconnected services: `mysql` (MySQL 8.0 with `mysql_data` volume & healthcheck), `redis` (Redis Alpine with `redis_data` volume & healthcheck), `backend` (Spring Boot Java 17 app), and `frontend` (React Nginx on port 5173/80).
  - Created [.env.example](file:///d:/CODES/SPRING%20BOOT/project/project/.env.example) with complete template variables for local Docker Compose and cloud deployments (Render/Railway). Full test suite passed (47 tests).
- 2026-08-19: Fixed Product `imageUrls` Lazy Initialization / Jackson Serialization Error:
  - Fixed `failed to lazily initialize a collection: could not initialize proxy - no Session (com.Project1.project.dto.response.ProductResponseDTO["imageUrls"])` when viewing product details.
  - Set `@ElementCollection(fetch = FetchType.EAGER)` on `Product.imageUrls` in [Product.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/entity/Product.java).
  - Updated [ProductMapper.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/mapper/ProductMapper.java) to defensively copy `entity.getImageUrls()` into a detached `new ArrayList<>(...)` so DTO contains standard serializable Java list.
- 2026-08-19: Phase 11 — Performance & Caching (Redis + Spring Cache):
  - Added `spring-boot-starter-cache` and `spring-boot-starter-data-redis` to [pom.xml](file:///d:/CODES/SPRING%20BOOT/project/project/pom.xml).
  - Added `@EnableCaching` to [ProjectApplication.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/ProjectApplication.java).
  - Configured Redis connection properties (`spring.data.redis.host/port/password`), cache type (`spring.cache.type=redis`), and default TTL in [application.properties](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/resources/application.properties), while configuring `spring.cache.type=simple` in [test application.properties](file:///d:/CODES/SPRING%20BOOT/project/project/src/test/resources/application.properties) for isolated in-memory test execution.
  - Created [RedisConfig.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/config/RedisConfig.java) providing `RedisCacheManager` with Jackson polymorphic JSON serialization (`GenericJackson2JsonRedisSerializer` with `JavaTimeModule`) and custom TTLs: `category` (1 hour), `categories` (1 hour), `product` (10 minutes), `products` (10 minutes).
  - Updated [CategoryResponseDTO.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/dto/response/CategoryResponseDTO.java) and [ProductResponseDTO.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/dto/response/ProductResponseDTO.java) to implement `java.io.Serializable`.
  - Added `@Cacheable(value = "category", key = "#id")` and cache eviction on create/update/delete in [CategoryServiceImpl.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/service/impl/CategoryServiceImpl.java).
  - Added `@Cacheable(value = "product", key = "#id")` and cache eviction on create/update/delete/image operations in [ProductServiceImpl.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/service/impl/ProductServiceImpl.java).
  - Added `@Caching(evict = ...)` on [ReviewServiceImpl.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/service/impl/ReviewServiceImpl.java) to evict stale product rating cache upon review submission.
  - Created [CacheIntegrationTest.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/test/java/com/Project1/project/CacheIntegrationTest.java) to verify `@Cacheable` and `@CacheEvict` cycles. Full test suite expanded to 47 passing tests.
- 2026-08-19: User-Relative Order Numbering on Customer Frontend:
  - Updated [OrderHistoryPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/OrderHistoryPage.jsx) to compute chronological user-relative order numbers (`page * pageSize + index + 1`) instead of displaying global database IDs. Oldest order = #1, newest = #N.
  - Updated [OrderDetailPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/OrderDetailPage.jsx) to receive `userOrderNumber` via React Router `location.state` and display it, with fallback to global ID when accessed directly via URL.
  - Admin orders page ([AdminOrdersPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/admin/AdminOrdersPage.jsx)) intentionally left unchanged — admins continue to see global database order IDs.
- 2026-08-19: SMTP Configuration Fix — Fixed `Authentication failed` error for Gmail email dispatch:
  - Changed default `spring.mail.host` in [application.properties](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/resources/application.properties) from `smtp.mailtrap.io` to `smtp.gmail.com`. Credentials were correct but were being sent to the wrong SMTP server.
- 2026-08-19: Phase 10 — Background Processing:
  - Created [OrderAutoCancelService.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/service/impl/OrderAutoCancelService.java) — `@Scheduled` job (cron: every 30 min) that finds `PLACED` orders older than configurable threshold (default 24h), cancels them, restores stock via `ProductRepository.restoreStock()`, and sends cancellation email.
  - Added `findByStatusAndCreatedAtBefore(OrderStatus, Instant)` to [OrderRepository.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/repository/OrderRepository.java) for time-based stale order queries.
  - Added `restoreStock(Long id, int qty)` to [ProductRepository.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/repository/ProductRepository.java) — atomic stock increment (inverse of `decreaseStockIfAvailable`).
  - Added `@EnableAsync` to [ProjectApplication.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/ProjectApplication.java).
  - Created [AsyncConfig.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/config/AsyncConfig.java) — `ThreadPoolTaskExecutor` bean (`emailTaskExecutor`) with core=2, max=5, queue=25, prefix `email-async-`.
  - Added `@Async("emailTaskExecutor")` to all 4 public methods in [EmailServiceImpl.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/service/impl/EmailServiceImpl.java) — email dispatch no longer blocks HTTP request threads.
  - Added configurable properties in [application.properties](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/resources/application.properties): `app.order.auto-cancel.threshold-hours` (default 24) and `app.order.auto-cancel.cron` (default every 30 min).
- 2026-08-19: Phase 9 — Production Features (Email Service, OTP Verification & Order Notifications):
  - Integrated `spring-boot-starter-mail` and configured `application.properties` with fallback logging.
  - Created `OtpType` enum, `OtpVerification` entity with SHA-256 hash storage, 15-minute expiration, and `OtpVerificationRepository`.
  - Added `emailVerified` boolean field to `User` entity and updated `DataInitializer` to ensure seeded accounts are verified.
  - Created `OtpService` & `OtpServiceImpl` with cryptographically secure 6-digit numeric generation (`SecureRandom`), hashing, validation, and single-use invalidation.
  - Created `EmailService` & `EmailServiceImpl` with styled HTML email templates for Signup Verification, Password Reset, Order Confirmation, and Order Status Updates, with robust console logging fallback when SMTP is unconfigured.
  - Implemented `/api/v1/auth/verify-email`, `/api/v1/auth/resend-otp`, `/api/v1/auth/forgot-password`, and `/api/v1/auth/reset-password` in `AuthService`/`AuthServiceImpl` and `AuthController`.
  - Injected `EmailService` into `OrderServiceImpl` to automatically dispatch Order Confirmation emails on checkout and Order Status Update emails on admin status transitions.
  - Built React frontend pages [VerifyEmailPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/VerifyEmailPage.jsx), [ForgotPasswordPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/ForgotPasswordPage.jsx), and [ResetPasswordPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/ResetPasswordPage.jsx) with 60-second cooldown timers and routes in `App.jsx`.
  - Created comprehensive unit tests in `OtpServiceImplTest` and `EmailServiceImplTest`, and updated `AuthServiceImplTest` and `OrderServiceImplTest`. Full suite expanded to 46 passing automated tests.
- 2026-08-19: Comprehensive Production Readiness & Error Audit:
  - Created and structured [error.md](file:///d:/CODES/SPRING%20BOOT/project/project/files/error.md) detailing 10 prioritized production-readiness gaps (P01–P10) covering database migrations (Flyway), HttpOnly refresh token cookies, rate limiting, actuator health probes, HikariCP tuning, Cloudinary retry resilience, sanitized 500 error bodies, and structured JSON logging.
  - Documented complete resolution audit history of 24 resolved security, concurrency, and validation defects (E01–E24).
- 2026-08-19: Multi-Category Catalog Expansion & Self-Healing Data Sync:
  - Seeded 15+ diverse products across 5 distinct categories (*Electronics & Computing*, *Audio & Wearables*, *Fashion & Apparel*, *Home & Kitchen*, *Books & Stationery*).
  - Created [DataInitializer.java](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/java/com/Project1/project/config/DataInitializer.java) (`@Profile("!test")`, `@Transactional` `CommandLineRunner`) with self-healing upsert routine that matches categories by exact name and repairs missing/empty `imageUrls` on startup without impacting tests.
  - Updated [data.sql](file:///d:/CODES/SPRING%20BOOT/project/project/src/main/resources/data.sql) with dynamic category update queries and `product_images` collection table population for direct MySQL Workbench/terminal execution.
  - Enhanced frontend image fallback UX in [ProductListPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/ProductListPage.jsx) and [ProductDetailPage.jsx](file:///d:/CODES/SPRING%20BOOT/project/project/frontend/src/pages/ProductDetailPage.jsx) with clean SVG/CSS fallback boxes.
- 2026-08-18: Phase 7 — Product Images & File Handling:
  - Added Cloudinary Java SDK dependency in `pom.xml`.
  - Configured multipart upload limits (5MB max file size, 10MB max request size) and Cloudinary properties in `application.properties`.
  - Created `InvalidFileException`, `FileUploadException`, and registered them in `GlobalExceptionHandler`.
  - Created `CloudinaryConfig` Spring configuration bean.
  - Created `FileStorageService` and `CloudinaryStorageServiceImpl` with MIME/size validation and secure URL generation.
  - Added `uploadProductImage` and `removeProductImage` to `ProductService`/`ProductServiceImpl`.
  - Added endpoints in `ProductController` (`POST /api/v1/products/{id}/images`, `DELETE /api/v1/products/{id}/images`) and `FileUploadController` (`POST /api/v1/files/upload`).
  - Updated React frontend `AdminProductsPage.jsx` with direct file upload, Cloudinary upload status, image thumbnail previews, and remove button.
  - Added unit and MockMvc tests in `CloudinaryStorageServiceImplTest` and `ProductControllerTest`.
- 2026-08-18: Updated `api-tests.http` with complete top-to-bottom testing sequence for IntelliJ HTTP Client: includes full Token Lifecycle (Registration, Login, Refresh Token Rotation, Re-use Prevention, Server-Side Logout Revocation, Invalid Credentials), Security/Authorization checks, Categories/Products CRUD & Filters, Cart limits & Cross-user isolation, Orders & IDOR checks, Order State Machine transitions, Live Aggregate Reviews, and Cleanup.
- 2026-08-18: Auth Hardening & Full Token Lifecycle:
  - **CRITICAL Fix**: Embedded `"role"` claim in backend JWT (`JwtUtil`) and updated `AuthContext.jsx` to parse the exact role claim, removing all email substring heuristics (`.includes('admin')`).
  - **HIGH Fix**: Connected refresh tokens on frontend — stored in `localStorage`, implemented automatic 401 response interceptor in `axios.js` for silent token rotation and transparent request retry.
  - **MEDIUM Fix**: Implemented `POST /api/v1/auth/logout` endpoint in Spring Boot (`AuthServiceImpl`) to revoke refresh tokens on logout; called from `AuthContext.logout()`.
  - **MEDIUM Fix**: Added automatic login on successful registration with token persistence.
  - **MEDIUM Fix**: Added `RefreshTokenCleanupService` with `@Scheduled` and `@EnableScheduling` to delete expired/revoked refresh tokens.
  - **MEDIUM Fix**: Extracted CORS allowed origins into `application.properties` (`cors.allowed-origins`) for clean environment configuration.
- 2026-08-18: Simplified Auth UI Layout — Replaced the dual top tabs with a single dedicated Sign In form by default, with a bottom inline link ("New user? Create an account") to switch to registration, and vice-versa ("Already have an account? Sign in here").
- 2026-08-18: Cleaned up Auth UI — Removed quick sample/demo account login and registration helper buttons from `LoginPage.jsx`.
- 2026-08-18: Updated Frontend Registration Flow — Registration no longer auto-logs the user in or auto-redirects to `/`. `AuthContext.register` now only performs API registration without saving session token; `LoginPage` switches to Sign In tab with prefilled email and success message; `RegisterPage` navigates to `/login` with success banner.
- 2026-08-18: Refined Phase 8.5 Auth flow: unauthenticated entry now routes to Login/Signup portal first. Connected directly to Spring Boot backend (`/auth/login` and `/auth/register`) with dynamic hostname detection and relaxed dev CORS in SecurityConfig.
- 2026-08-17: Implemented Phase 8.5 (Frontend with React + Vite + Axios + React Router + Tailwind). Rebuilt clean, simple frontend matching PRD/Phases specs. Connected directly to Spring Boot backend API with Axios JWT interceptor, added product catalog with Spring Page pagination, product details with reviews, cart & checkout, order history with snapshot lines, login/register, and admin panel with order state machine transitions.
- 2026-08-16: Frontend scope decision (React) formalized across prd.md, phases.md,
  design.md.
- 2026-08-16: Fixed E20 - Duplicate email in registration now returns 409 Conflict
  using `EmailAlreadyExistsException`.
- 2026-08-16: Spot-checked E15 - Verified `ReviewMapper` has no empty catch blocks.
- 2026-08-16: Security hardening — restricted product/category write endpoints to
  ADMIN and allowed only GET publicly (updated SecurityConfig, ProductController,
  CategoryController).
- 2026-08-16: Prevented IDOR on orders — enforced ownership check in
  OrderServiceImpl.getOrderById (only owner or ADMIN can view).
- 2026-08-16: Exceptions & error mapping — expanded GlobalExceptionHandler to map
  business exceptions (404, 400, 409, 401, 403) and added EmptyCartException,
  ForbiddenOperationException, InsufficientStockException.
- 2026-08-16: AuthRefactor — extracted AuthService and AuthServiceImpl;
  AuthController now delegates to service.
- 2026-08-16: Stock handling and checkout safety — added stock checks in
  CartServiceImpl (rejects additions exceeding stock, and later also rejects
  updateCartItem quantity increases exceeding stock), added conditional DB update in
  ProductRepository (decreaseStockIfAvailable) and used it in OrderServiceImpl to
  atomically decrement stock during checkout; trims/clears cart on success.
- 2026-08-16: Data integrity — added DB-level unique constraint on
  CartItem(cart_id, product_id) to prevent duplicate rows under concurrency.
- 2026-08-16: Reviews & ratings improvements — computed averageRating via
  ReviewRepository query and included it in Product responses; removed silent
  exception swallowing in ProductMapper/ReviewMapper.
- 2026-08-16: Extracted CurrentUserProvider to centralize current-user resolution
  (used by Cart/Order/Review services).

## Status
- Build: successful (backend package verified; frontend `npm run build` verified).
- Backend: Phases 0–13 complete. 47 passing automated tests (0 failures, 0 errors).
- Frontend: Phase 8.5 completed and running on http://localhost:5173 with Cloudinary image upload in Admin panel. Customer order history now shows user-relative order numbers.
- Security & Hardening: JWT Stateless auth + HttpOnly SameSite refresh token cookies, IP-based sliding window rate limiting, BCrypt password hashing, Flyway versioned migrations.
- Email: Gmail SMTP configured and operational; dispatched asynchronously via `@Async` thread pool.
- Background Jobs: Order auto-cancellation (24h threshold, 30-min cron) and refresh token cleanup (daily 2 AM) active.
- Performance & Caching: Redis Cache layer configured with Jackson JSON serialization and automatic `@CacheEvict` invalidation with silent DB fallback (`CacheErrorHandler`).
- Docker & Containers: Full-stack Docker Compose setup (`docker-compose.yml`, backend Dockerfile, frontend Dockerfile with Nginx SPA routing, `.env.example`).
- Production Readiness & Observability: Spring Actuator health probes, HikariCP connection pool hardening with leak detection, Cloudinary upload retry resilience, sanitized 500 error envelopes.
- Documentation: Comprehensive [README.md](file:///d:/CODES/SPRING%20BOOT/project/project/README.md), [error.md](file:///d:/CODES/SPRING%20BOOT/project/project/files/error.md), and [walkthrough.md](file:///C:/Users/SOHAM%20GULAME/.gemini/antigravity-ide/brain/431d2dd4-d285-4230-9d06-6efb1556dfff/walkthrough.md).
- Project Status: 🎉 **100% COMPLETE (All 13 Phases & Production Audits Delivered)**.

---

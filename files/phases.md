# phases.md — Build Order

Build strictly in this order. Do not start a phase until the previous one is confirmed
working. Wait for the user to say "next phase" before moving on.

---

## Phase 0 — Project Setup
- Initialize Spring Boot project (Maven, Java, Spring Web, Spring Data JPA, Spring
  Security, MySQL/PostgreSQL driver, Lombok, Validation)
- Set up `application.properties`/`.yml` with DB connection
- Confirm the app boots with a health-check endpoint

## Phase 1 — Core Entities & Category Module
*(uses: JPA, REST — already learned)*
- Create `Category` entity + repository
- Category CRUD (admin-only create/update/delete, public list/get)
- DTOs + mapper for Category
- Global exception handler skeleton (`CategoryNotFoundException`, etc.)

## Phase 2 — Product Catalog
*(uses: JPA relationships, pagination/search/filter — already learned)*
- Create `Product` entity (ManyToOne → Category)
- Product CRUD (admin-only create/update/delete)
- Public: paginated product listing
- Public: search by name/description
- Public: filter by category, price range, availability
- DTOs + mapper for Product (response DTO includes category name, not full object)

## Phase 3 — Authentication & Users
*(uses: Spring Security, JWT — already learned)*
- `User` entity, roles: CUSTOMER, ADMIN
- Register, login, JWT access + refresh token
- Password encoding
- Secure all Phase 1–2 admin endpoints with role checks
- Profile get/update endpoint, scoped to the JWT principal

## Phase 4 — Cart
*(uses: JPA relationships, JWT-scoped data access — already learned)*
- `Cart` and `CartItem` entities, tied to User
- Add to cart, update quantity, remove item, view cart with computed total
- All cart access resolved from JWT, never from a path variable

## Phase 5 — Orders
*(uses: JPA relationships, DTO/mapper, business logic — already learned)*
- `Order` and `OrderItem` entities
- Convert cart → order on checkout (snapshot product name/price at time of order)
- Customer: view own orders (paginated), view single order
- Admin: view all orders (filterable by status), update order status
- Order status state machine: PLACED → CONFIRMED → SHIPPED → DELIVERED / CANCELLED

## Phase 6 — Reviews & Ratings
*(uses: JPA relationships, validation — already learned)*
- `Review` entity (Product ManyToOne, User ManyToOne, rating 1–5, comment)
- Add review (only if customer has an order containing that product)
- One review per user per product (DB constraint + service check)
- Public: paginated reviews per product, average rating on product detail

## Phase 7 — Product Images
*(uses: File Handling — new learning phase, learn concepts before building)*
- Multipart upload endpoint for product images (admin only)
- File validation (type, size)
- Cloud storage integration with Cloudinary (upload, secure URL generation) instead of local disk
- Product response DTO includes image URL(s)

## Phase 8 — Testing
*(uses: JUnit 5, Mockito, MockMvc — new learning phase, learn concepts before building)*
- Unit tests: cart total calculation, order status transitions, review constraint logic
- Controller tests with MockMvc for at least Product and Order endpoints
- Integration test for the full checkout flow (cart → order)

## Phase 8.5 — Frontend (React) 🆕
*(added 2026-08-16 — see `prd.md` §6a for scope. Numbered 8.5 rather than renumbering
everything after it, so existing references to Phase 9+ in `memory.md`/`error.md`
still point to the right thing.)*

**Backend prerequisite (do this first, it's a backend change):**
- Add CORS configuration to `SecurityConfig` for the frontend's dev origin
  (`http://localhost:5173` for Vite's default port)

**8.5.1 — Project setup**
- Scaffold with Vite (`npm create vite@latest` → React template)
- Install Axios, React Router, Tailwind CSS
- Set up an Axios instance with a base URL and a request interceptor that attaches
  the JWT from storage to every request

**8.5.2 — Auth**
- Login page, Register page
- Store JWT client-side (React state/context to start; discuss localStorage
  trade-offs before using it — token is sensitive)
- Auth context/hook so any component can check "am I logged in, and what role"
- Protected route wrapper — redirect unauthenticated users to login, redirect
  customers away from admin-only routes

**8.5.3 — Product catalog (customer-facing)**
- Product listing page — paginated, matches the backend's pagination shape
- Search bar + category/price filters, calling the existing filter query params
- Product detail page — shows description, price, stock, average rating, reviews
- "Add to cart" action, disabled/handled gracefully when `InsufficientStockException`
  comes back from the API

**8.5.4 — Cart & Checkout**
- Cart page — list items, update quantity, remove item, show computed total
- Checkout action — calls `POST /orders`, handles the "empty cart" 400 case in the UI
- Order confirmation view after successful checkout

**8.5.5 — Order history**
- "My Orders" page — paginated list, matches backend order history endpoint
- Order detail page — items, status, total

**8.5.6 — Reviews**
- Review form on product detail page (only shown/enabled if the customer has
  ordered the product — mirror the backend's `NotOrderedException` rule in the UI,
  but the backend check remains the source of truth)
- Display existing reviews + average rating

**8.5.7 — Admin panel**
- Category management (create/edit/delete)
- Product management (create/edit/delete, including stock quantity)
- Order management — view all orders, filter by status, update status (respecting
  the backend's valid state-transition rules — surface `InvalidOrderStatusTransitionException`
  errors clearly rather than letting the UI allow an invalid jump)

**8.5.8 — Polish**
- Loading states, error states (map backend error responses to readable UI messages)
- Basic responsive layout
- README section covering how to run frontend + backend together locally

## Phase 9 — Production Features
*(uses: Email Service, OTP — new learning phase, learn concepts before building)*
- Email verification on signup
- Forgot password / reset password flow
- Order confirmation + status update emails

## Phase 10 — Background Processing
*(uses: @Scheduled, @Async — new learning phase, learn concepts before building)*
- Auto-cancel orders left unconfirmed after a set time window
- Async email sending (don't block the request thread)

## Phase 11 — Performance
*(uses: Redis, Spring Cache — new learning phase, learn concepts before building)*
- Cache product listing and category list
- `@CacheEvict` on product/category create/update/delete

## Phase 12 — Docker & Deployment
*(uses: Docker, Deployment — new learning phase, learn concepts before building)*
- Dockerfile for the app
- Docker Compose with DB (and Redis if Phase 11 is done)
- Deploy to Render/Railway with environment-based profiles

## Phase 13 — Polish & Review
- Full pass over all endpoints: consistent status codes, consistent error shape,
  validation on every request DTO
- README with setup instructions and API overview
- Final review against `rules.md` for architecture consistency

---

### Notes for the agent
- Every backend phase before Phase 7 uses only concepts already completed in the
  learner's Spring Boot roadmap. Do not introduce out-of-scope backend concepts (e.g.
  Redis, Docker) inside an earlier phase "for convenience."
- Phase 8.5 (Frontend) is a separate learning track (React), not gated by the Spring
  Boot roadmap — normal React/JS conventions apply there.
- After finishing a phase, update `memory.md` with what was built and any decisions
  made, before starting the next phase.
- Phase 7 (Product Images / File Handling) was deferred in favor of Testing (Phase 8)
  and is still pending — don't assume it's done just because later phases are.

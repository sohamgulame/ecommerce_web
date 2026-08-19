# design.md — Technical Design

## 1. Tech Stack

**Backend**
- **Language**: Java
- **Framework**: Spring Boot
- **Persistence**: Spring Data JPA + Hibernate
- **Database**: MySQL, local instance (decided — see Open Design Decisions)
- **Security**: Spring Security + JWT (access token implemented; refresh token still
  open — see `error.md` E11)
- **Build tool**: Maven
- **File storage** (Phase 7+): Cloudinary (locked in)
- **Caching** (Phase 11+): Redis, Spring Cache abstraction
- **Containerization** (Phase 12+): Docker, Docker Compose
- **Testing**: JUnit 5, Mockito, MockMvc — implemented (Phase 8, complete)

**Frontend** *(added 2026-08-16 — see `prd.md` §6a)*
- **Framework**: React
- **Build tool**: Vite
- **HTTP client**: Axios, with a request interceptor attaching the JWT to every call
- **Routing**: React Router
- **Styling**: Tailwind CSS
- Deliberately excludes Next.js/Redux/state-management libraries — out of scope for
  this project's size; React Context is enough for auth state.

## 2. Entity-Relationship Overview

```
Category (1) ──── (M) Product
Product  (1) ──── (M) Review
User     (1) ──── (M) Review
User     (1) ──── (1) Cart
Cart     (1) ──── (M) CartItem ──── (M:1) Product
User     (1) ──── (M) Order
Order    (1) ──── (M) OrderItem ──── (M:1) Product [price/name snapshot]
```

## 3. Core Entities

**User**
- id, name, email (unique), password (encoded), role (CUSTOMER/ADMIN), phone, address

**Category**
- id, name (unique), description

**Product**
- id, name, description, price, stockQuantity, imageUrl(s), category (ManyToOne)

**Cart**
- id, user (OneToOne)
- items: List\<CartItem\> (OneToMany)

**CartItem**
- id, cart (ManyToOne), product (ManyToOne), quantity

**Order**
- id, user (ManyToOne), status (enum), totalAmount, createdAt
- items: List\<OrderItem\> (OneToMany)

**OrderItem**
- id, order (ManyToOne), productId, productNameSnapshot, priceSnapshot, quantity
- (snapshot fields exist so historical orders don't change if product price/name
  changes later)

**Review**
- id, product (ManyToOne), user (ManyToOne), rating (1–5), comment, createdAt
- unique constraint on (product_id, user_id)

## 4. DTO Shape Examples

**ProductResponseDTO**
```
id, name, description, price, stockQuantity, categoryName, averageRating, imageUrls
```
— never expose the raw Category entity; flatten to `categoryName`.

**OrderResponseDTO**
```
id, status, totalAmount, createdAt, items: [ { productName, price, quantity } ]
```
— items come from the OrderItem snapshot fields, not a live Product join.

**CartResponseDTO**
```
items: [ { productId, productName, price, quantity, subtotal } ], totalAmount
```
— `totalAmount` is computed in the service layer, not stored redundantly on Cart.

## 5. API Design Conventions
- Base path: `/api/v1/...`
- Public endpoints: `GET /api/v1/products`, `GET /api/v1/products/{id}`,
  `GET /api/v1/categories`, `GET /api/v1/products/{id}/reviews`
- Customer endpoints (JWT required, role CUSTOMER): `/api/v1/cart/**`,
  `/api/v1/orders/**` (scoped to self), `/api/v1/reviews`
- Admin endpoints (JWT required, role ADMIN): `POST/PUT/DELETE /api/v1/products`,
  `POST/PUT/DELETE /api/v1/categories`, `GET /api/v1/admin/orders`,
  `PUT /api/v1/admin/orders/{id}/status`
- Pagination: `?page=0&size=20&sort=createdAt,desc` on all list endpoints
- Filtering example: `GET /api/v1/products?categoryId=3&minPrice=100&maxPrice=5000`

## 6. Order Status State Machine
```
PLACED → CONFIRMED → SHIPPED → DELIVERED
PLACED → CANCELLED
CONFIRMED → CANCELLED
```
Enforce valid transitions in the service layer — reject invalid jumps (e.g.
DELIVERED → PLACED) with a clear exception.

## 7. Security Design
- JWT filter validates token on every request, sets authentication in
  `SecurityContext`.
- `@PreAuthorize("hasRole('ADMIN')")` on admin-only controller methods.
- For "my own data" endpoints (cart, orders, profile), the service layer resolves the
  user from `SecurityContextHolder`, not from any client-supplied ID.
- **CORS** *(new requirement, added 2026-08-16 with the frontend)*: `SecurityConfig`
  must allow the frontend's dev origin (`http://localhost:5173`) for credentialed
  requests carrying the `Authorization` header. Didn't exist before since there was no
  browser-based client. Restrict allowed origins to the actual frontend origin(s) —
  don't use a wildcard `*` alongside the `Authorization` header, browsers will reject
  that combination anyway, and it's bad practice even where they wouldn't.

## 7a. Frontend Architecture *(added 2026-08-16)*
- **Auth flow**: login/register call the existing `/auth/**` endpoints, store the
  returned `accessToken`, attach it to subsequent requests via an Axios interceptor
  (`Authorization: Bearer <token>`).
- **Route protection**: a wrapper component checks auth state (and role, for admin
  routes) before rendering; unauthenticated users are redirected to `/login`.
- **State management**: no global state library — React Context for auth
  (current user + token), local component state / React Query-free `useEffect` +
  `useState` for data fetching per page. Revisit only if this becomes unwieldy.
- **Error handling**: backend error responses follow a consistent shape (`status`,
  `message`, `timestamp`, `path` — per `rules.md` §4); the frontend should surface
  `message` directly in the UI for expected business errors (409 duplicate, 400
  insufficient stock, etc.) rather than a generic "something went wrong."
- **Pagination contract**: backend list endpoints return Spring's standard `Page<T>`
  shape (`content`, `totalPages`, `totalElements`, `number`, etc.) — frontend list
  views should consume this shape directly rather than assuming a plain array.

## 8. Open Design Decisions (fill in as the project progresses)
- Database choice: **DECIDED — MySQL, running locally.** Not Supabase — Supabase's
  value (auth, storage, realtime, auto-APIs) is redundant here since Spring Boot owns
  auth, business logic, and (later) file storage itself. Local MySQL avoids network
  latency during development/testing and matches the Docker Compose setup planned for
  Phase 12. Keeps consistency with the learner's existing demo project, which also
  uses MySQL.
- Whether Category supports subcategories (self-referencing) or stays flat: _TBD_
- Whether stock quantity decrements happen at order-placement or order-confirmation:
  _TBD_
- Payment status simulation approach (simple enum vs mock gateway call): _TBD_

Log the final decision for each of these in `memory.md` once made.
